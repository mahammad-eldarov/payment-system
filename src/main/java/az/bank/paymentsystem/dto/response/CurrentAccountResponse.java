package az.bank.paymentsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.CurrentAccountStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentAccountResponse {

    private Integer id;
    private String accountNumber;
    private String currentAccountHolderName;
    private BigDecimal balance;
    private Currency currency;
    private CurrentAccountStatus status;
    private LocalDate activationDate;
    private LocalDate expiryDate;

    private List<TransactionResponse> transactions;

}
