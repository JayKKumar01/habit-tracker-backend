package com.jay.habit_tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habit_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // ✅ Many-to-Many mappedBy (inverse side)
    @ManyToMany(mappedBy = "tags")
    private Set<Habit> habits = new HashSet<>();
}
