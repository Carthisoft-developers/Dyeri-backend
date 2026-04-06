// com/dyeri/core/domain/entities/Review.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("reviews")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class Review {
    @Id private UUID id;
    @Column("author_id") private UUID authorId;
    @Column("cook_id")   private UUID cookId;
    @Column("dish_id")   private UUID dishId;
    private Integer rating;
    private String text;
    @CreatedDate @Column("created_at") private Instant createdAt;
}
