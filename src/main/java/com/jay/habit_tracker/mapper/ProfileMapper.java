package com.jay.habit_tracker.mapper;

import com.jay.habit_tracker.dto.profile.ProfileRequest;
import com.jay.habit_tracker.dto.profile.ProfileResponse;
import com.jay.habit_tracker.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileResponse toDto(Profile profile);
}
