package com.jay.habit_tracker.controller;

import com.jay.habit_tracker.dto.habit_log.HabitLogDto;
import com.jay.habit_tracker.service.HabitLogService;
import com.jay.habit_tracker.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/habit-log")
@RequiredArgsConstructor
public class HabitLogController {

    private final HabitLogService habitLogService;
    private final JwtUtil jwtUtil;

    @PostMapping("/update/{userId}")
    public ResponseEntity<?> updateHabitLog(
            @PathVariable Long userId,
            @RequestBody HabitLogDto habitLogDto,
            HttpServletRequest request
    ) {
        Long tokenUserId = jwtUtil.extractUserId(request); // You’ll implement this
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        HabitLogDto updatedLog = habitLogService.updateHabitLog(habitLogDto);
        if (updatedLog == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Habit not found or not owned by user"));
        }

        return ResponseEntity.ok(updatedLog);
    }
}
