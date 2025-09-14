package com.jay.habit_tracker.repository;

import com.jay.habit_tracker.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
