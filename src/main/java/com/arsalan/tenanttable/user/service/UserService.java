package com.arsalan.tenanttable.user.service;

import com.arsalan.tenanttable.user.dto.AllUsersResponseDto;
import com.arsalan.tenanttable.user.entity.User;
import com.arsalan.tenanttable.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AllUsersResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> AllUsersResponseDto.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .toList();
    }
}