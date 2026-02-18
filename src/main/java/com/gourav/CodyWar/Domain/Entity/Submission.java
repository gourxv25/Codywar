package com.gourav.CodyWar.Domain.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    private Integer executionTimeMs;  // Execution time in milliseconds

    private Integer memoryUsedKb;  // Memory used in KB

    private Integer testCasesPassed;

    private Integer totalTestCases;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;  // Compilation/runtime error message

    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    private Instant judgedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = Instant.now();
    }
}

