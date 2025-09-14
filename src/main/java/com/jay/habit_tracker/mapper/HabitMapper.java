package com.jay.habit_tracker.mapper;

import com.jay.habit_tracker.dto.habit.HabitRequest;
import com.jay.habit_tracker.dto.habit.HabitResponse;
import com.jay.habit_tracker.entity.Habit;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.HashSet;

@Mapper(componentModel = "spring")
public interface HabitMapper {

    @Mapping(source = "targetDays", target = "targetDays")
    @Mapping(source = "tags", target = "tags")
    HabitResponse toDto(Habit habit);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // set manually in service
    Habit toEntity(HabitRequest dto);
}
