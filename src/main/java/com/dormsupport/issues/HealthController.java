package com.dormsupport.issues;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "issues-service", "status", "ok");
    }

    @GetMapping("/api/v1/issues")
    public List<Object> issues() {
        return List.of();
    }
}
