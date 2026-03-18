// com/dyeri/core/domain/entities/Favorite.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.relational.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("favorites")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Favorite {
    @Column("client_id") private UUID clientId;
    @Column("dish_id")   private UUID dishId;
    @Column("saved_at")  private Instant savedAt;
}
