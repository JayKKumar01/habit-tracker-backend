package com.jay.habit_tracker.dto.habit;

import com.jay.habit_tracker.enums.Frequency;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitRequest {
    private String title;
    private String description;
    private Frequency frequency;
    private Set<String> targetDays;
    private LocalDate startDate;
}
