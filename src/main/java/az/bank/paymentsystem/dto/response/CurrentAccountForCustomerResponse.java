package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentAccountForCustomerResponse {
    private String accountNumber;
    private String currentAccountHolderName;
    private BigDecimal balance;
    private Currency currency;
    private CurrentAccountStatus status;
    private LocalDate activationDate;
    private LocalDate expiryDate;
}
