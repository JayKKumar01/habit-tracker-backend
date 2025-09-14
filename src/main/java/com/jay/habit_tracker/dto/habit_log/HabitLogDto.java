package com.jay.habit_tracker.dto.habit_log;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitLogDto {
    private Long habitId;
    private LocalDate date;
    private boolean completed;
}
