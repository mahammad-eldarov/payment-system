package az.bank.paymentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.PaymentStatus;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment")
public class PaymentEntity extends BaseEntity {

    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @NotNull
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Size(max = 500)
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @NotNull
    @Column(name = "from_type", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private PaymentSourceType fromType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id")
    private CardEntity fromCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private CurrentAccountEntity fromAccount;

    @NotNull
    @Column(name = "to_type", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private PaymentSourceType toType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id")
    private CardEntity toCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private CurrentAccountEntity toAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name = "to_external_party_id")
    private ExternalPartyEntity toExternalParty;


}
