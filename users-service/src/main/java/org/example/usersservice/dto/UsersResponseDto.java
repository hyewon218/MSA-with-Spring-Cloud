package org.example.usersservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.example.usersservice.entity.Users;

@Data
@Builder
// @JsonInclude 을 사용함으로서, null 인 필드 null 로 노출 x
// 회원 조회와 주문 목록 조회 응답 dto 클래스로 동시 사용 O
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsersResponseDto {

    private String email;
    private String name;
    private String userId;

    private List<OrdersResponseDto> orders; // 주문 목록 반환

    public static UsersResponseDto of(Users users) {
        return UsersResponseDto.builder()
            .email(users.getEmail())
            .name(users.getName())
            .userId(users.getUserId())
            .build();
    }
}