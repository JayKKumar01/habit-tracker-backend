package com.jay.habit_tracker.service;

import com.jay.habit_tracker.dto.tag.TagDto;
import com.jay.habit_tracker.entity.Tag;
import com.jay.habit_tracker.enums.Frequency;

import java.util.Map;

public interface TagService {
    TagDto addHabitTag(Long habitId, String name);
    void removeHabitTag(Long habitId, Long tagId);
    Map<Frequency, Tag> ensureDefaultTags();
}
