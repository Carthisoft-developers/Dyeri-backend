// com/cuisinvoisin/domain/entities/Client.java
package com.cuisinvoisin.domain.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("CLIENT")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Client extends User {
    // No extra fields; role discriminator is CLIENT
}
