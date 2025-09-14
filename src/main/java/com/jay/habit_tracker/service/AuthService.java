package com.jay.habit_tracker.service;

import com.jay.habit_tracker.dto.AuthRequest;
import com.jay.habit_tracker.dto.user.UserResponse;
import com.jay.habit_tracker.dto.user.UserRegistration;

public interface AuthService {
    UserResponse signup(UserRegistration dto);
    String login(AuthRequest request);
    void sendVerificationCode(String email);
    boolean verifyCode(String email, String code);
}
