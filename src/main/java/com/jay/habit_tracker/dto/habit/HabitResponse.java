package com.jay.habit_tracker.dto.habit;

import com.jay.habit_tracker.dto.habit_log.HabitLogDto;
import com.jay.habit_tracker.dto.tag.TagDto;
import com.jay.habit_tracker.enums.Frequency;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitResponse {
    private Long id;
    private String title;
    private String description;
    private Frequency frequency;
    private Set<DayOfWeek> targetDays; // e.g., ["MONDAY", "WEDNESDAY"]
    private LocalDate startDate;
    private LocalDate endDate;

    // ✅ List of associated entries
    private List<HabitLogDto> logs;
    private List<TagDto> tags;
}
