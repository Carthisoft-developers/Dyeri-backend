// com/cuisinvoisin/domain/entities/Favorite.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "favorites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@IdClass(Favorite.FavoritePK.class)
public class Favorite {

    @Id
    @Column(name = "client_id", columnDefinition = "uuid")
    private UUID clientId;

    @Id
    @Column(name = "dish_id", columnDefinition = "uuid")
    private UUID dishId;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @Embeddable
    public record FavoritePK(UUID clientId, UUID dishId) implements Serializable {}
}
