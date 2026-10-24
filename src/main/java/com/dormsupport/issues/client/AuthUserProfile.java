package com.dormsupport.issues.client;

import java.time.Instant;
import java.util.UUID;

public record AuthUserProfile(
        UUID id,
        String login,
        String fullName,
        String role,
        UUID blockId,
        String roomNumber,
        Instant createdAt) {
}
