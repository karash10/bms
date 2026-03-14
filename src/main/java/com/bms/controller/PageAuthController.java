package com.bms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bms.entity.User;
import com.bms.entity_enums.UserRole;
import com.bms.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class PageAuthController {

    private static final String JWT_COOKIE_NAME = "BMS_TOKEN";

    private final AuthService authService;

    public PageAuthController(AuthService authService) {
        this.authService = authService;
    }

    // ========== Login ==========

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {

        try {
            String token = authService.login(email, password);

            // Set JWT as HttpOnly cookie
            Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 24 hours
            response.addCookie(cookie);

            // If HTMX request, return a redirect header
            response.setHeader("HX-Redirect", "/");
            return "fragments/login-success";

        } catch (Exception e) {
            model.addAttribute("error", "Invalid email or password");
            // Return partial HTML for HTMX target
            return "fragments/alert-error";
        }
    }

    // ========== Register ==========

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            HttpServletResponse response,
            Model model) {

        try {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(UserRole.valueOf(role));

            authService.register(user);

            // Auto-login after registration
            String token = authService.login(email, password);

            Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            response.setHeader("HX-Redirect", "/");
            return "fragments/register-success";

        } catch (Exception e) {
            String message = "Registration failed";
            if (e.getMessage() != null && e.getMessage().contains("Unique")) {
                message = "An account with this email already exists";
            }
            model.addAttribute("error", message);
            return "fragments/alert-error";
        }
    }

    // ========== Logout ==========

    @PostMapping("/logout")
    public String handleLogout(HttpServletResponse response) {
        // Clear the JWT cookie
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete cookie
        response.addCookie(cookie);

        return "redirect:/login";
    }
}
