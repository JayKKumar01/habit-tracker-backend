package com.jay.habit_tracker.service;


import com.jay.habit_tracker.dto.user.UserResponse;

public interface UserService {
    UserResponse getUserByEmail(String email);
}
