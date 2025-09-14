package com.jay.habit_tracker.controller;

import com.jay.habit_tracker.dto.AuthRequest;
import com.jay.habit_tracker.dto.AuthResponse;
import com.jay.habit_tracker.dto.user.UserRegistration;
import com.jay.habit_tracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRegistration dto) {
        return ResponseEntity.status(201).body(authService.signup(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String email = body.get("email");

        if (email == null || email.isBlank()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            authService.sendVerificationCode(email);
            response.put("success", true);
            response.put("message", "Verification code sent");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send verification code");
            return ResponseEntity.internalServerError().body(response);
        }
    }



    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");

        boolean isVerified = authService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Email verified"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid or expired verification code"));
        }
    }


}
