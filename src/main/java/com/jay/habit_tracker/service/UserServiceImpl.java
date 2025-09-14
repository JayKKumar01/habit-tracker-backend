package com.jay.habit_tracker.service;

import com.jay.habit_tracker.dto.user.UserResponse;
import com.jay.habit_tracker.mapper.UserMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public UserResponse getUserByEmail(String email) {
        Object[] row = (Object[]) entityManager
                .createNativeQuery("SELECT id, name, email, created_at FROM users WHERE email = :email LIMIT 1")
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (row == null) return null;

        Instant createdAt = extractInstant(row[3]);

        return UserResponse.builder()
                .id(((Number) row[0]).longValue())
                .name((String) row[1])
                .email((String) row[2])
                .createdAt(createdAt)
                .build();
    }

    /**
     * Safely converts any SQL/PG type (Timestamp, Instant, OffsetDateTime) to Instant
     */
    private Instant extractInstant(Object columnValue) {
        if (columnValue == null) return null;

        if (columnValue instanceof Instant instant) {
            return instant;
        } else if (columnValue instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        } else if (columnValue instanceof OffsetDateTime odt) {
            return odt.toInstant();
        } else if (columnValue instanceof ZonedDateTime zdt) {
            return zdt.toInstant();
        } else {
            throw new IllegalArgumentException("Unexpected created_at type: " + columnValue.getClass());
        }
    }
}
