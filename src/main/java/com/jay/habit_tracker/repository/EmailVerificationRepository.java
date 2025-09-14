package com.jay.habit_tracker.repository;

import com.jay.habit_tracker.entity.EmailVerification;
import com.jay.habit_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

}
