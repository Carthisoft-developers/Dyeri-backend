// com/dyeri/core/domain/entities/SavedAddress.java
package com.dyeri.core.domain.entities;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("saved_addresses")
@Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class SavedAddress {
    @Id private UUID id;
    @Column("client_id") private UUID clientId;
    private String label;
    private String address;
    @Column("additional_info") private String additionalInfo;
    private Double latitude;
    private Double longitude;
    @Column("is_default") private Boolean defaultAddress;
    @CreatedDate @Column("created_at") private Instant createdAt;
}
