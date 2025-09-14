package com.jay.habit_tracker.mapper;

import com.jay.habit_tracker.dto.AuthRequest;
import com.jay.habit_tracker.dto.user.UserResponse;
import com.jay.habit_tracker.dto.user.UserRegistration;
import com.jay.habit_tracker.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);                            // for responses
    User toEntity(UserRegistration dto);
}
