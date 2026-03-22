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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "current_account")
public class CurrentAccountEntity extends BaseEntity {

    @Size(max = 18)
    @NotNull
    @Column(name = "account_number", nullable = false, length = 18)
    private String accountNumber;

    @NotNull
    @Column(name = "current_account_holder_name", nullable = false, length = Integer.MAX_VALUE)
    private String currentAccountHolderName;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CurrentAccountStatus status;

    @NotNull
    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private CustomerEntity customer;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;


}