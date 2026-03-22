package az.bank.paymentsystem.entity;

import az.bank.paymentsystem.enums.CardBrand;
import az.bank.paymentsystem.enums.CardName;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_order")
public class CardOrderEntity extends BaseEntity {

    @Column(name = "status", length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "rejection_reason", length = Integer.MAX_VALUE)
    private String rejectionReason;

    @Column(name = "card_holder_name", length = Integer.MAX_VALUE)
    private String cardHolderName;

    @Column(name = "card_brand", length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CardBrand cardBrand;

    @Column(name = "card_name", length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CardName cardName;

    @NotNull(message = "Currency cannot be null")
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "card_type", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Size(min = 4, max = 4)
    @Pattern(regexp = "^[0-9]{4}$")
    @Column(name = "password", length = 4)
    private String password;

}