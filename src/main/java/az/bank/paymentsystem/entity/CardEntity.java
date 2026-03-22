package az.bank.paymentsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.CardBrand;
import az.bank.paymentsystem.enums.CardName;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card")
public class CardEntity extends BaseEntity {

    @NotNull
    @Column(name = "card_holder_name", nullable = false, length = Integer.MAX_VALUE)
    private String cardholderName;

    @NotNull
    @Column(name = "card_name", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CardName cardName;

    @Size(max = 16)
    @NotNull
    @Column(name = "pan", nullable = false, length = 16)
    private String pan;

    @NotNull
    @Column(name = "cvv", nullable = false, length = Integer.MAX_VALUE)
    private String cvv;

    @NotNull
    @NotBlank
    @Size(min = 4, max = 4)
    @Pattern(regexp = "^[0-9]{4}$")
    @Column(name = "password", nullable = false, length = 4)
    private String password;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @NotNull(message = "Currency cannot be null")
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @NotNull
    @Column(name = "card_brand", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CardBrand cardBrand;

    @NotNull
    @Column(name = "card_type", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @NotNull
    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private CustomerEntity customer;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;


}