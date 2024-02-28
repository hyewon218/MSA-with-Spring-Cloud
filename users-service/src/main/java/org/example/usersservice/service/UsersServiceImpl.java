package org.example.usersservice.service;

import lombok.RequiredArgsConstructor;
import org.example.usersservice.dto.UsersCreateRequestDto;
import org.example.usersservice.dto.UsersCreateResponseDto;
import org.example.usersservice.repository.UsersRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    @Override
    public UsersCreateResponseDto createUser(UsersCreateRequestDto requestDto) {
        String encryptedPassword = this.bcryptPasswordEncoder.encode(requestDto.getPassword());
        return UsersCreateResponseDto.of(usersRepository.save(requestDto.toEntity(encryptedPassword)));
    }
}