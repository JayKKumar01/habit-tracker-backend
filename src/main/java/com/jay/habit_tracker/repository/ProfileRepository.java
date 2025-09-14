package com.jay.habit_tracker.repository;

import com.jay.habit_tracker.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

}
