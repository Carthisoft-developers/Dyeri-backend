package com.dyeri.payment.entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Table("cook_payouts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CookPayout {
    @Id private UUID id;
    @Column("cook_id") private UUID cookId;
    @Column("order_id") private UUID orderId;
    @Column("gross_amount") private BigDecimal grossAmount;
    @Column("platform_fee") private BigDecimal platformFee;
    @Column("net_amount") private BigDecimal netAmount;
    @Column("paid_at") private Instant paidAt;
}