// com/dyeri/core/domain/entities/Follow.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.relational.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("follows")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Follow {
    @Column("client_id")   private UUID clientId;
    @Column("cook_id")     private UUID cookId;
    @Column("followed_at") private Instant followedAt;
}
