package org.example.usersservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.usersservice.entity.Users;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UsersCreateRequestDto {

    @Email(message = "This is not an email format")
    @NotBlank(message = "Email Cannot be null")
    private String email;

    @NotBlank(message = "Name Cannot be null")
    @Size(min = 2, message = "Name Cannot be less than two characters")
    private String name;

    @NotBlank(message = "Password Cannot be null")
    @Size(min = 8, message = "Password Cannot ne less than two characters")
    private String password;

    public Users toEntity(String encryptedPwd) {
        return Users.builder()
            .email(email)
            .name(name)
            .encryptedPwd(encryptedPwd)
            .userId(UUID.randomUUID().toString())
            .build();
    }
}