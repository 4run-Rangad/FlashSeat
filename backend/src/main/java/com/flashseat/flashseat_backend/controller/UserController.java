package com.flashseat.flashseat_backend.controller;

import com.flashseat.flashseat_backend.dto.UserCreateRequest;
import com.flashseat.flashseat_backend.dto.UserResponse;
import com.flashseat.flashseat_backend.dto.UserUpdateRequest;
import com.flashseat.flashseat_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody UserCreateRequest request
            ) {
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
            ) {
        return userService.updateUser(userId, request);
    }
}
