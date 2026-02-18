package com.gourav.CodyWar.Domain.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "battles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String roomCode;  // For private rooms

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BattleStatus status = BattleStatus.WAITING;

    @Column(nullable = false)
    @Builder.Default
    private int maxParticipants = 2;  // Default 1v1

    @Column(nullable = false)
    @Builder.Default
    private int durationSeconds = 1800;  // 30 minutes default

    @Column(nullable = false)
    @Builder.Default
    private boolean isPrivate = false;

    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<BattleParticipant> participants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Submission> submissions = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant finishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
