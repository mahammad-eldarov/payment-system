package az.bank.paymentsystem.entity;

import az.bank.paymentsystem.enums.OrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_rate_limit")
public class OrderRateLimitEntity extends BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
//    private Integer id;

    @Column(name = "order_type", length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @ColumnDefault("0")
    @Column(name = "rejection_count")
    private Integer rejectionCount;

    @Column(name = "cooldown_until")
    private Instant cooldownUntil;

//    @Column(name = "created_at")
//    private Instant createdAt;
//
//    @Column(name = "updated_at")
//    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    public OrderRateLimitEntity(CustomerEntity customer, OrderType orderType) {
        this.customer = customer;
        this.orderType = orderType;
        this.rejectionCount = 0;
    }
}