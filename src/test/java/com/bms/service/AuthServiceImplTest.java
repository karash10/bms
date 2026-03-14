package com.bms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bms.entity.User;
import com.bms.entity_enums.UserRole;
import com.bms.repository.UserRepository;
import com.bms.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_shouldEncodePasswordAndSave() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("plaintext");
        user.setRole(UserRole.USER);

        when(passwordEncoder.encode("plaintext")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.register(user);

        assertThat(result.getPassword()).isEqualTo("encoded-hash");
        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(user);
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-hash");
        setId(user, 1L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-hash")).thenReturn(true);
        when(jwtService.generateToken("test@example.com")).thenReturn("jwt-token-123");

        String token = authService.login("test@example.com", "password");

        assertThat(token).isEqualTo("jwt-token-123");
    }

    @Test
    void login_withInvalidPassword_shouldThrow() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-hash");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("test@example.com", "wrong"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void login_withNonexistentEmail_shouldThrow() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nobody@example.com", "password"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    private void setId(Object entity, Long id) {
        try {
            var clazz = entity.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, id);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
