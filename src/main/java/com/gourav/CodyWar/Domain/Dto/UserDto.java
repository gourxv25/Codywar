package com.gourav.CodyWar.Domain.Dto;

import com.gourav.CodyWar.Domain.Entity.Role;
import com.gourav.CodyWar.Domain.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String username;
    private String email;
    private int ratingScore;
    private int battlesPlayed;
    private int battlesWon;
    private Set<String> languagesUsed;
    private Role role;
    private Status status;
    private Instant createdAt;
    private Instant lastLoginAt;
}

