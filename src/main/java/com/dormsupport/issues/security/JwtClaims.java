package com.dormsupport.issues.security;

import java.util.UUID;

public record JwtClaims(UUID userId, String role) {
}
