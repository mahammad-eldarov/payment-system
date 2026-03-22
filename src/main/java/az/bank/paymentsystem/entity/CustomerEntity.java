package az.bank.paymentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.CustomerStatus;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer")
public class CustomerEntity extends BaseEntity {

    @NotNull
    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @NotNull
    @Column(name = "surname", nullable = false, length = Integer.MAX_VALUE)
    private String surname;

    @NotNull
    @Column(name = "pin", nullable = false, length = 7)
    private String pin;

    @NotNull
    @Column(name = "email", nullable = false, length = Integer.MAX_VALUE)
    private String email;

    @NotNull
    @Column(name = "phone_number", nullable = false, length = Integer.MAX_VALUE)
    private String phoneNumber;

    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    @ColumnDefault("true")
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<CardEntity> cardEntity;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<CurrentAccountEntity> currentAccountEntity;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<TransactionEntity> transactionEntity;






}