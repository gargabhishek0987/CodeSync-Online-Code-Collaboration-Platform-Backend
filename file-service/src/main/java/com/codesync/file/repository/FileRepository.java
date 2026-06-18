package com.codesync.file.repository;

import com.codesync.file.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<CodeFile, Long> {
    List<CodeFile> findByProjectIdAndIsDeletedFalse(Long projectId);
    Optional<CodeFile> findByProjectIdAndPathAndIsDeletedFalse(Long projectId, String path);
    boolean existsByProjectIdAndPath(Long projectId, String path);
}
