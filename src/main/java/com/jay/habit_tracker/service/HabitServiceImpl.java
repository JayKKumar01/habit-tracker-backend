package com.jay.habit_tracker.service;

import com.jay.habit_tracker.dto.habit.*;
import com.jay.habit_tracker.dto.habit_log.HabitLogDto;
import com.jay.habit_tracker.dto.tag.TagDto;
import com.jay.habit_tracker.entity.Habit;
import com.jay.habit_tracker.entity.Tag;
import com.jay.habit_tracker.entity.User;
import com.jay.habit_tracker.enums.Frequency;
import com.jay.habit_tracker.mapper.HabitMapper;
import com.jay.habit_tracker.repository.HabitRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final HabitMapper habitMapper;
    private final EntityManager entityManager;
    private final TagService tagService;

    @Override
    @Transactional
    public HabitResponse createHabit(Long userId, HabitRequest habitRequest) {
        User userRef = entityManager.getReference(User.class, userId);

        // Ensure tags exist and retrieve correct one
        Map<Frequency, Tag> defaultTags = tagService.ensureDefaultTags();
        Tag frequencyTag = defaultTags.get(habitRequest.getFrequency());

        Habit habit = habitMapper.toEntity(habitRequest);
        habit.setUser(userRef);
        habit.setTags(Set.of(frequencyTag));

        Habit savedHabit = habitRepository.save(habit);
        return habitMapper.toDto(savedHabit);
    }



    @Override
    @Transactional
    public HabitUpdate updateHabit(HabitUpdate updateDto) {
        // ✅ Use COALESCE to avoid overwriting with nulls if fields are null
        StringBuilder sql = new StringBuilder("""
        UPDATE habits
        SET title = COALESCE(:title, title),
            description = COALESCE(:description, description)
    """);

        if (updateDto.getEndDate() != null) {
            sql.append(", end_date = :endDate");
        }

        sql.append(" WHERE id = :habitId");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("title", updateDto.getTitle());
        query.setParameter("description", updateDto.getDescription());
        query.setParameter("habitId", updateDto.getHabitId());

        if (updateDto.getEndDate() != null) {
            query.setParameter("endDate", updateDto.getEndDate());
        }

        int updatedRows = query.executeUpdate();

        if (updatedRows == 0) {
            throw new IllegalStateException("No habit found with id " + updateDto.getHabitId());
        }

        return updateDto;
    }


    @Override
    public List<HabitResponse> getHabitsByUserId(Long userId) {
        String sql = """
        SELECT h.id, h.title, h.description, h.frequency,
               STRING_AGG(DISTINCT htds.target_day, ',') AS target_days,
               h.start_date, h.end_date,
               hl.date AS log_date, hl.completed AS log_completed,
               STRING_AGG(DISTINCT ht.id::text, ',') AS tag_ids,
               STRING_AGG(DISTINCT ht.name, ',') AS tag_names
        FROM habits h
        LEFT JOIN habit_target_day_strings htds ON h.id = htds.habit_id
        LEFT JOIN habit_logs hl ON hl.habit_id = h.id
        LEFT JOIN habit_tag_mapping htm ON h.id = htm.habit_id
        LEFT JOIN habit_tags ht ON ht.id = htm.tag_id
        WHERE h.user_id = :userId
        GROUP BY h.id, h.title, h.description, h.frequency, h.start_date, h.end_date, hl.date, hl.completed
        ORDER BY h.id, hl.date DESC
    """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId)
                .getResultList();

        Map<Long, HabitResponse> habitMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long habitId = ((Number) row[0]).longValue();
            String targetDaysRaw = (String) row[4];

            HabitResponse habit = habitMap.get(habitId);
            if (habit == null) {
                Set<DayOfWeek> targetDays = (targetDaysRaw == null || targetDaysRaw.isBlank())
                        ? EnumSet.noneOf(DayOfWeek.class)
                        : EnumSet.copyOf(
                        Arrays.stream(targetDaysRaw.split(","))
                                .map(String::trim)
                                .map(DayOfWeek::valueOf)
                                .collect(Collectors.toSet())
                );

                habit = HabitResponse.builder()
                        .id(habitId)
                        .title((String) row[1])
                        .description((String) row[2])
                        .frequency(Frequency.valueOf((String) row[3]))
                        .targetDays(targetDays)
                        .startDate(extractLocalDate(row[5]))
                        .endDate(row[6] == null ? null : extractLocalDate(row[6]))
                        .logs(new ArrayList<>())
                        .tags(new ArrayList<>())
                        .build();

                habitMap.put(habitId, habit);

                // ✅ Attach tags (if any)
                Object tagIdObj = row[9];
                Object tagNameObj = row[10];
                if (tagIdObj != null && tagNameObj != null) {
                    String[] tagIds = ((String) tagIdObj).split(",");
                    String[] tagNames = ((String) tagNameObj).split(",");

                    List<TagDto> tagDtos = new ArrayList<>(tagIds.length);
                    for (int i = 0; i < tagIds.length; i++) {
                        tagDtos.add(TagDto.builder()
                                .id(Long.parseLong(tagIds[i].trim()))
                                .name(tagNames[i].trim())
                                .build());
                    }
                    habit.setTags(tagDtos);
                }
            }

            // ✅ Add logs (if present)
            Object logDateObj = row[7];
            Object completedObj = row[8];
            if (logDateObj != null && completedObj != null) {
                habit.getLogs().add(HabitLogDto.builder()
                        .habitId(habitId)
                        .date(extractLocalDate(logDateObj))
                        .completed((Boolean) completedObj)
                        .build());
            }
        }

        // ✅ Trim lists to save memory
        habitMap.values().forEach(habit -> {
            if (habit.getLogs() instanceof ArrayList<?>) {
                ((ArrayList<?>) habit.getLogs()).trimToSize();
            }
            if (habit.getTags() instanceof ArrayList<?>) {
                ((ArrayList<?>) habit.getTags()).trimToSize();
            }
        });

        return new ArrayList<>(habitMap.values());
    }

    /**
     * ✅ Safe date extractor for PostgreSQL results.
     */
    private LocalDate extractLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Date sqlDate) return sqlDate.toLocalDate();
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (value instanceof java.time.LocalDate ld) return ld;
        if (value instanceof java.time.LocalDateTime ldt) return ldt.toLocalDate();
        throw new IllegalArgumentException("Unexpected date type: " + value.getClass());
    }

    @Override
    public boolean deleteHabit(Long habitId) {
        Habit habitRef = entityManager.getReference(Habit.class,habitId);
        try {
            habitRepository.delete(habitRef);
            return true;
        }catch (Exception e){
            return false;
        }
    }


}
