package org.example.usersservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.usersservice.dto.UsersRequestDto;
import org.example.usersservice.dto.UsersResponseDto;
import org.example.usersservice.entity.Users;
import org.example.usersservice.repository.UsersRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users = usersRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return new User(users.getEmail(), users.getEncryptedPwd(),
            true, true, true, true,
            new ArrayList<>());
    }

    @Override
    @Transactional
    public UsersResponseDto createUser(UsersRequestDto requestDto) {
        String encryptedPassword = this.bcryptPasswordEncoder.encode(requestDto.getPassword());
        return UsersResponseDto.of(usersRepository.save(requestDto.toEntity(encryptedPassword)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsersResponseDto> getAllUsers() {
        return this.usersRepository.findAll()
            .stream()
            .map(UsersResponseDto::of)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsersResponseDto getUserByUserId(String userId) {
        return UsersResponseDto.of(this.usersRepository.findByUserId(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found")));
    }
}