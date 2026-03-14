package com.bms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bms.entity.User;
import com.bms.repository.UserRepository;
import com.bms.security.JwtService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public User register(User user) {
        log.info("Registering user: email={}", user.getEmail());

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        User saved = userRepository.save(user);
        log.info("User registered: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    public String login(String email, String password) {
        log.info("Login attempt: email={}", email);

        User user =
                userRepository.findByEmail(email).orElseThrow();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed: invalid password for email={}", email);
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(email);
        log.info("Login successful: email={}", email);
        return token;
    }
}
