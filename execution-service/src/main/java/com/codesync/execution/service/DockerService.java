package com.codesync.execution.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.async.ResultCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerService {

    private final DockerClient dockerClient;

    private static final Map<String, String> LANGUAGE_IMAGE_MAP = Map.of(
            "python", "python:3.9-slim",
            "javascript", "node:16-slim",
            "java", "eclipse-temurin:17-jdk-alpine"
    );

    public Map<String, String> executeCode(String language, String code) throws Exception {
        String image = LANGUAGE_IMAGE_MAP.get(language.toLowerCase());
        if (image == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        // Use a fixed filename for simplicity in the container, 
        // Java 11+ single-file execution doesn't strictly require the filename to match the class name
        String fileName = getFileName(language, "Main");
        
        java.nio.file.Path tempDir = Files.createTempDirectory("codesync-exec");
        File tempFile = new File(tempDir.toFile(), fileName);
        Files.writeString(tempFile.toPath(), code);

        String containerId = null;
        try {
            String[] runCmd = getRunCommand(language, fileName);
            log.info("Creating container with image: {} and command: {}", image, String.join(" ", runCmd));

            CreateContainerResponse container = dockerClient.createContainerCmd(image)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(256 * 1024 * 1024L) // 256MB
                            .withCpuQuota(100000L) // 100% of one core
                            .withAutoRemove(false))
                    .withWorkingDir("/app")
                    .withCmd(runCmd)
                    .exec();

            containerId = container.getId();

            // Create the /app directory and copy the file directly into it
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withHostResource(tempFile.getAbsolutePath())
                    .withRemotePath("/app/")
                    .exec();

            // Start container
            dockerClient.startContainerCmd(containerId).exec();

            // Wait for completion (with timeout)
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(containerId).exec(waitCallback);
            boolean finished = waitCallback.awaitCompletion(20, TimeUnit.SECONDS);
            
            if (!finished) {
                log.warn("Job timed out after 20 seconds");
                return Map.of("stdout", "", "stderr", "Execution timed out.");
            }

            // Collect logs
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(false)
                    .withTailAll()
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame item) {
                            if (item.getStreamType() == com.github.dockerjava.api.model.StreamType.STDOUT) {
                                stdout.write(item.getPayload(), 0, item.getPayload().length);
                            } else if (item.getStreamType() == com.github.dockerjava.api.model.StreamType.STDERR) {
                                stderr.write(item.getPayload(), 0, item.getPayload().length);
                            }
                        }
                    }).awaitCompletion();

            String stdoutStr = stdout.toString().trim();
            String stderrStr = stderr.toString().trim();
            
            log.info("Job result - stdout: '{}', stderr: '{}'", stdoutStr, stderrStr);
            return Map.of(
                    "stdout", stdoutStr,
                    "stderr", stderrStr
            );

        } catch (Exception e) {
            log.error("Container execution failed: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            try {
                Files.deleteIfExists(tempDir);
            } catch (IOException e) {
                log.warn("Failed to delete temp dir: {}", e.getMessage());
            }
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    log.warn("Failed to remove container {}: {}", containerId, e.getMessage());
                }
            }
        }
    }

    private String getFileName(String language, String className) {
        return switch (language.toLowerCase()) {
            case "python" -> "script.py";
            case "javascript" -> "script.js";
            case "java" -> "Main.java";
            default -> "script.txt";
        };
    }

    private String[] getRunCommand(String language, String fileName) {
        return switch (language.toLowerCase()) {
            case "python" -> new String[]{"python", "/app/" + fileName};
            case "javascript" -> new String[]{"node", "/app/" + fileName};
            case "java" -> new String[]{"java", "/app/" + fileName};
            default -> new String[]{"cat", "/app/" + fileName};
        };
    }
}
