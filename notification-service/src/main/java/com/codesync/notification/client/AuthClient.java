package com.codesync.notification.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/auth/users/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);

    @Data
    @Builder
    class UserDto {
        private Long id;
        private String username;
        private String email;
    }
}
