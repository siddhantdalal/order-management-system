package com.orderflow.orderservice.controller;

import com.orderflow.common.dto.ApiResponse;
import com.orderflow.common.dto.UserDto;
import com.orderflow.orderservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@RequestHeader("X-User-Email") String email) {
        UserDto user = authService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = authService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
