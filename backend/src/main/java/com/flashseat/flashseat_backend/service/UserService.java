package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.UserCreateRequest;
import com.flashseat.flashseat_backend.dto.UserResponse;
import com.flashseat.flashseat_backend.entity.User;
import com.flashseat.flashseat_backend.exception.EmailAlreadyExistsException;
import com.flashseat.flashseat_backend.exception.UserNotFoundException;
import com.flashseat.flashseat_backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "Email " + request.email() + " is already registered"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordHash)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User " + userId + " not found"
                ));
        return toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse updateUser(
            Long userId,
            UserUpdateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User " + userId + " not found"
                ));

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {

            throw new EmailAlreadyExistsException(
                    "Email " + request.email() + " is already registered"
            );
        }

        user.setEmail(request.email());

        String passwordHash = passwordEncoder.encode(request.password());
        user.setPasswordHash(passwordHash);

        user.setUpdatedAt(OffsetDateTime.now());

        User updatedUser = userRepository.save(user);

        return toUserResponse(updatedUser);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
