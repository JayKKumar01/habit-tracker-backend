package com.jay.habit_tracker.dto.tag;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDeleteRequest {
    private Long habitId;
    private Long tagId;
}
