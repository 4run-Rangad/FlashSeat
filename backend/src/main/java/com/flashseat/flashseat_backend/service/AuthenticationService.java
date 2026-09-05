package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.LoginRequest;
import com.flashseat.flashseat_backend.dto.LoginResponse;
import com.flashseat.flashseat_backend.entity.User;
import com.flashseat.flashseat_backend.exception.InvalidCredentialsException;
import com.flashseat.flashseat_backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token);
    }
}
