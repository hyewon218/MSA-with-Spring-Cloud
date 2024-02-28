package org.example.usersservice.dto;

import lombok.Builder;
import lombok.Data;
import org.example.usersservice.entity.Users;

@Data
@Builder
public class UsersCreateResponseDto {

    private String email;
    private String name;
    private String userId;

    public static UsersCreateResponseDto of(Users users) {
        return UsersCreateResponseDto.builder()
            .email(users.getEmail())
            .name(users.getName())
            .userId(users.getUserId())
            .build();
    }
}