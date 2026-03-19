package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentAccountOrderResponse {
    private Integer id;
    private String accountNumber;
    private String currentAccountHolderName;
    private BigDecimal balance;
    private Currency currency;
    private CurrentAccountStatus status;
    private LocalDate activationDate;
    private LocalDate expiryDate;
}
