package com.dyeri.notification.entity;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;
import java.time.Instant;
import java.util.UUID;
@Table("notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id private UUID id;
    @Column("user_id") private UUID userId;
    private String type;
    private String title;
    private String body;
    @Column("is_read") private boolean read;
    @CreatedDate @Column("created_at") private Instant createdAt;
}