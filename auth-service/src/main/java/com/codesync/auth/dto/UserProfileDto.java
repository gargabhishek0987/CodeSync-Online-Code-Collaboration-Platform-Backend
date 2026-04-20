package com.codesync.auth.dto;

import com.codesync.auth.entity.Provider;
import com.codesync.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private Provider provider;
    private String avatarUrl;
    private String bio;
}
