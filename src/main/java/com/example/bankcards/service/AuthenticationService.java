package com.example.bankcards.service;

import com.example.bankcards.dto.JwtAuthenticationResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.SignUpRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username " + request.username() + " is already taken");
        }

        Role role = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Role ROLE_USER not found"));

        User user = User.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .roles(Set.of(role))
            .build();

        userRepository.save(user);

        String jwt = jwtService.generateJwt(user);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new IllegalArgumentException("Invalid username"));

        String jwt = jwtService.generateJwt(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
