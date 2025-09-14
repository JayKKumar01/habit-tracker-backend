package com.jay.habit_tracker.service;

import com.jay.habit_tracker.dto.tag.TagDto;
import com.jay.habit_tracker.entity.Tag;
import com.jay.habit_tracker.enums.Frequency;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final EntityManager entityManager;

    /**
     * ✅ Ensure that 'daily' and 'weekly' tags exist in DB.
     * If they don't exist, create them.
     * Returns a map of tagName -> Tag entity.
     */
    @Override
    public Map<Frequency, Tag> ensureDefaultTags() {
        Map<Frequency, Tag> result = new HashMap<>();

        for (Frequency frequency : Frequency.values()) {
            String tagName = frequency.name().toLowerCase(); // enum → "daily" / "weekly"
            Tag tag;
            try {
                tag = entityManager.createQuery(
                                "SELECT t FROM Tag t WHERE t.name = :name", Tag.class)
                        .setParameter("name", tagName)
                        .getSingleResult();
            } catch (NoResultException e) {
                tag = Tag.builder().name(tagName).build();
                entityManager.persist(tag);
            }
            result.put(frequency, tag);
        }

        return result;
    }


    @Override
    @Transactional
    public TagDto addHabitTag(Long habitId, String name) {
        String normalizedTag = name.trim().toLowerCase();

        Tag tag;
        try {
            tag = entityManager.createQuery(
                            "SELECT t FROM Tag t WHERE t.name = :name", Tag.class)
                    .setParameter("name", normalizedTag)
                    .getSingleResult();
        } catch (NoResultException e) {
            tag = Tag.builder().name(normalizedTag).build();
            entityManager.persist(tag);
        }

        // ✅ PostgreSQL way: Insert or ignore if conflict exists
        String insertSql = """
            INSERT INTO habit_tag_mapping (habit_id, tag_id)
            VALUES (:habitId, :tagId)
            ON CONFLICT (habit_id, tag_id) DO NOTHING
        """;

        entityManager.createNativeQuery(insertSql)
                .setParameter("habitId", habitId)
                .setParameter("tagId", tag.getId())
                .executeUpdate();

        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    @Override
    @Transactional
    public void removeHabitTag(Long habitId, Long tagId) {
        String deleteSql = """
            DELETE FROM habit_tag_mapping
            WHERE habit_id = :habitId AND tag_id = :tagId
        """;

        entityManager.createNativeQuery(deleteSql)
                .setParameter("habitId", habitId)
                .setParameter("tagId", tagId)
                .executeUpdate();
    }
}
