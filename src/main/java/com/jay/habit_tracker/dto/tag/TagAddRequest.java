package com.jay.habit_tracker.dto.tag;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagAddRequest {
    private Long habitId;
    private String name;
}
