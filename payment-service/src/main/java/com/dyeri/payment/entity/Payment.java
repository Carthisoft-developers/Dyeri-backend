package com.dyeri.payment.entity;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Table("payments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id private UUID id;
    @Column("order_id") private UUID orderId;
    @Column("client_id") private UUID clientId;
    private BigDecimal amount;
    private String currency;
    private String provider;
    @Column("provider_payment_id") private String providerPaymentId;
    private String status;
    @CreatedDate @Column("created_at") private Instant createdAt;
    @LastModifiedDate @Column("updated_at") private Instant updatedAt;
}