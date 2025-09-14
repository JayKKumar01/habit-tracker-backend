package com.jay.habit_tracker.service;

import com.jay.habit_tracker.dto.profile.ProfileRequest;
import com.jay.habit_tracker.dto.profile.ProfileResponse;
import com.jay.habit_tracker.entity.Profile;
import com.jay.habit_tracker.mapper.ProfileMapper;
import com.jay.habit_tracker.repository.ProfileRepository;
import com.jay.habit_tracker.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ProfileRequest saveOrUpdate(Long userId, ProfileRequest profileRequest) {
        // ✅ Upsert into profiles (bio) → PostgreSQL syntax
        entityManager.createNativeQuery("""
            INSERT INTO profiles (user_id, bio)
            VALUES (:userId, :bio)
            ON CONFLICT (user_id) 
            DO UPDATE SET bio = EXCLUDED.bio
        """)
                .setParameter("userId", userId)
                .setParameter("bio", profileRequest.getBio())
                .executeUpdate();

        // ✅ Update users (name)
        entityManager.createNativeQuery("""
            UPDATE users
            SET name = :name
            WHERE id = :userId
        """)
                .setParameter("name", profileRequest.getName())
                .setParameter("userId", userId)
                .executeUpdate();

        return profileRequest;
    }

    @Override
    public ProfileResponse getProfile(Long userId) {
        Profile profile = entityManager.createQuery("""
            SELECT p FROM Profile p
            WHERE p.user.id = :userId
        """, Profile.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        return (profile == null) ? null : profileMapper.toDto(profile);
    }
}
