package az.bank.paymentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "external_party")
public class ExternalPartyEntity extends BaseEntity {

    @Size(max = 10)
    @NotNull
    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @Size(max = 100)
    @NotNull
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Size(max = 100)
    @NotNull
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Size(max = 16)
    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Size(max = 20)
    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Size(max = 15)
    @Column(name = "phone", length = 15)
    private String phone;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 4)
    @Column(name = "pin", length = 4)
    private String pin;

    @Size(max = 20)
    @NotNull
    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;


}