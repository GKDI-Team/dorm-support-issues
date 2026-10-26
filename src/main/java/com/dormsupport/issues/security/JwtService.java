package com.dormsupport.issues.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final Pattern SUBJECT = Pattern.compile("\\\"sub\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern ROLE = Pattern.compile("\\\"role\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern EXPIRATION = Pattern.compile("\\\"exp\\\"\\s*:\\s*(\\d+)");

    private final byte[] secret;

    public JwtService(@Value("${auth.jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public JwtClaims parse(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3
                    || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), DECODER.decode(parts[2]))) {
                throw new InvalidTokenException();
            }

            String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher subject = SUBJECT.matcher(payload);
            Matcher role = ROLE.matcher(payload);
            Matcher expiration = EXPIRATION.matcher(payload);
            if (!subject.find() || !role.find() || !expiration.find()
                    || Long.parseLong(expiration.group(1)) <= Instant.now().getEpochSecond()) {
                throw new InvalidTokenException();
            }
            return new JwtClaims(UUID.fromString(subject.group(1)), role.group(1));
        } catch (RuntimeException exception) {
            throw new InvalidTokenException();
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to verify JWT", exception);
        }
    }
}
