package com.jay.habit_tracker.controller;

import com.jay.habit_tracker.dto.profile.ProfileRequest;
import com.jay.habit_tracker.dto.profile.ProfileResponse;
import com.jay.habit_tracker.service.ProfileService;
import com.jay.habit_tracker.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    // ✅ Create or update profile bio (secured)
    @PostMapping("/save/{userId}")
    public ResponseEntity<?> saveOrUpdateProfile(
            @PathVariable Long userId,
            @RequestBody ProfileRequest profileRequest,
            HttpServletRequest request
    ) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        ProfileRequest response = profileService.saveOrUpdate(userId, profileRequest);
        return ResponseEntity.ok(response);
    }

    // ✅ Get profile by userId (secured)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProfile(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
}
