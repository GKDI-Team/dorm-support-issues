package com.dormsupport.issues.client;

import java.time.Instant;
import java.util.UUID;

public record AuthUserProfile(
        UUID id,
        String login,
        String fullName,
        String role,
        String block,
        String roomNumber,
        Instant createdAt) {

    /** Compatibility constructor for existing test fixtures. */
    public AuthUserProfile(UUID id, String login, String fullName, String role,
                           UUID blockId, String roomNumber, Instant createdAt) {
        this(id, login, fullName, role,
                blockId == null ? null : blockId.toString(), roomNumber, createdAt);
    }
}
