package com.jay.habit_tracker.controller;

import com.jay.habit_tracker.dto.tag.TagAddRequest;
import com.jay.habit_tracker.dto.tag.TagDeleteRequest;
import com.jay.habit_tracker.dto.tag.TagDto;
import com.jay.habit_tracker.service.TagService;
import com.jay.habit_tracker.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final JwtUtil jwtUtil;

    @PostMapping("/add-habit-tag/{userId}")
    public ResponseEntity<?> addHabitTag(
            @PathVariable Long userId,
            @RequestBody TagAddRequest tagRequest,
            HttpServletRequest request
    ) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        try {
            TagDto addedTag = tagService.addHabitTag(tagRequest.getHabitId(), tagRequest.getName());
            return ResponseEntity.status(201).body(addedTag);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/remove-habit-tag/{userId}")
    public ResponseEntity<?> removeHabitTag(
            @PathVariable Long userId,
            @RequestBody TagDeleteRequest tagRequest,
            HttpServletRequest request
    ) {
        Long tokenUserId = jwtUtil.extractUserId(request);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        try {
            tagService.removeHabitTag(tagRequest.getHabitId(), tagRequest.getTagId());
            return ResponseEntity.ok(Map.of("message", "Tag removed from habit"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }


}
