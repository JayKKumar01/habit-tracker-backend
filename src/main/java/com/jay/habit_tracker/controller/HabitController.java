package com.jay.habit_tracker.controller;

import com.jay.habit_tracker.dto.habit.*;
import com.jay.habit_tracker.service.HabitService;
import com.jay.habit_tracker.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final JwtUtil jwtUtil;

    // ✅ Create habit (secured)
    @PostMapping("/create/{userId}")
    public ResponseEntity<?> createHabit(@PathVariable Long userId, @RequestBody HabitRequest requestDTO, HttpServletRequest request) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        HabitResponse created = habitService.createHabit(userId, requestDTO);
        return ResponseEntity.status(201).body(created);
    }

    // ✅ Edit habit (secured)
    @PutMapping("/edit/{userId}")
    public ResponseEntity<?> updateHabit(@PathVariable Long userId, @RequestBody HabitUpdate editRequest, HttpServletRequest request) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        HabitUpdate updated = habitService.updateHabit(editRequest);
        return ResponseEntity.ok(updated); // 200 OK for updates
    }



    @GetMapping("/habits/{userId}")
    public ResponseEntity<?> getHabitsByUserId(@PathVariable Long userId, HttpServletRequest request) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<HabitResponse> habits = habitService.getHabitsByUserId(userId);
        return ResponseEntity.ok(habits);
    }

    @DeleteMapping("/delete/{userId}/{habitId}")
    public ResponseEntity<?> deleteHabit(@PathVariable Long userId,
                                         @PathVariable Long habitId,
                                         HttpServletRequest request) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        boolean deleted = habitService.deleteHabit(habitId);
        if (!deleted) {
            return ResponseEntity.status(404).body(Map.of("error", "Habit not found or not authorized"));
        }

        return ResponseEntity.ok(Map.of("message", "Habit deleted", "habitId", habitId));
    }

}
