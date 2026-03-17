// com/cuisinvoisin/domain/entities/Follow.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follows")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@IdClass(Follow.FollowPK.class)
public class Follow {

    @Id
    @Column(name = "client_id", columnDefinition = "uuid")
    private UUID clientId;

    @Id
    @Column(name = "cook_id", columnDefinition = "uuid")
    private UUID cookId;

    @Column(name = "followed_at", nullable = false)
    private Instant followedAt;

    @Embeddable
    public record FollowPK(UUID clientId, UUID cookId) implements Serializable {}
}
