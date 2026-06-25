package com.arsalan.tenanttable.user.controller;

import com.arsalan.tenanttable.common.dto.ApiResponse;
import com.arsalan.tenanttable.user.dto.AllUsersResponseDto;
import com.arsalan.tenanttable.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AllUsersResponseDto>>> getAllUsers(
            HttpServletRequest request
    ) {
        List<AllUsersResponseDto> users = userService.getAllUsers();
        ApiResponse<List<AllUsersResponseDto>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Users fetched successfully",
                users,
                request.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }
}
