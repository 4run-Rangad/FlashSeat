package com.flashseat.flashseat_backend.controller;

import com.flashseat.flashseat_backend.dto.UserCreateRequest;
import com.flashseat.flashseat_backend.dto.UserResponse;
import com.flashseat.flashseat_backend.dto.UserUpdateRequest;
import com.flashseat.flashseat_backend.entity.User;
import com.flashseat.flashseat_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize("hasRole('USER')")
    public UserResponse getUserById(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();
        Long authenticatedUserId = authenticatedUser.getId();
        return userService.getUserById(userId, authenticatedUserId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
            ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();

        Long authenticatedUserId = authenticatedUser.getId();
        return userService.updateUser(userId, request, authenticatedUserId);
    }
}
