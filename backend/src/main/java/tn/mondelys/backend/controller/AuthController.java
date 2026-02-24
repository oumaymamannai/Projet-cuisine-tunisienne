package tn.mondelys.backend.controller;

import tn.mondelys.backend.dto.AuthDtos;
import tn.mondelys.backend.service.AdminAuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AdminAuthService adminAuthService;

    public AuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request, HttpServletResponse response) {
        AuthDtos.LoginResponse loginResponse = adminAuthService.login(request);

        Cookie authCookie = new Cookie("adminToken", loginResponse.getToken());
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(authCookie);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie authCookie = new Cookie("adminToken", "");
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    public ResponseEntity<Void> session(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.noContent().build();
    }
}
