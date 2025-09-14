package com.jay.habit_tracker.controller;

import com.jay.habit_tracker.dto.user.UserResponse;
import com.jay.habit_tracker.service.UserService;
import com.jay.habit_tracker.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getUserByEmail(@RequestParam String email, HttpServletRequest request) {
        String tokenEmail = jwtUtil.extractEmail(request);
        if (tokenEmail == null || !tokenEmail.equals(email)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        UserResponse userResponse = userService.getUserByEmail(email);
        if (userResponse == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User with email " + email + " not found."));
        }

        return ResponseEntity.ok(userResponse);
    }
}
