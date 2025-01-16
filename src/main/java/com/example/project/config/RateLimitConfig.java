package com.example.project.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucket);
    }

    private Bucket newBucket(String apiKey) {
        // Define bandwidth limits based on API key or plan
        return Bucket.builder()
            .addLimit(getLimit(apiKey))
            .build();
    }

    private Bandwidth getLimit(String apiKey) {
        return switch (apiKey) {
            case "premium" ->  Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofMinutes(1)).build();
            case "standard" -> Bandwidth.builder().capacity(40).refillIntervally(40, Duration.ofMinutes(1)).build();
            default ->  Bandwidth.builder().capacity(2).refillIntervally(2, Duration.ofMinutes(1)).build();
        };
    }

}