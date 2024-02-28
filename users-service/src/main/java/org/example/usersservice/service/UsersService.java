package org.example.usersservice.service;

import org.example.usersservice.dto.UsersCreateRequestDto;
import org.example.usersservice.dto.UsersCreateResponseDto;

public interface UsersService {

    UsersCreateResponseDto createUser(UsersCreateRequestDto requestDto);
}