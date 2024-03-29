package org.example.usersservice.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class UsersDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private Date createdAt;

    private String encryptedPwd;

    private List<OrdersResponseDto> orders;
}
