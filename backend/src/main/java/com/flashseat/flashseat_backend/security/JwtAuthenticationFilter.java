package com.flashseat.flashseat_backend.security;

import com.flashseat.flashseat_backend.entity.User;
import com.flashseat.flashseat_backend.repository.UserRepository;
import com.flashseat.flashseat_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Service
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            if (email != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                Optional<User> userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {

                    User user = userOptional.get();

                    if (jwtService.isTokenValid(token, user.getEmail())) {

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        Collections.emptyList()
                                );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            //invalid or expired jwt
            e.printStackTrace();
        }


        filterChain.doFilter(request, response);
    }
}
