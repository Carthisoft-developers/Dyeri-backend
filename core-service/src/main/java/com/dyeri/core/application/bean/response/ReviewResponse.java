package com.dyeri.core.application.bean.response;
import java.time.Instant;
import java.util.UUID;
public record ReviewResponse(UUID id, String authorName, String authorAvatar, int rating, String text, Instant createdAt) {}
