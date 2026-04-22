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
import az.bank.paymentsystem.enums.TinStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TinResponse {

    private Integer id;
    private String tinNumber;
    private String tinHolderName;
    private BigDecimal balance;
    private Currency currency;
    private TinStatus status;
    private LocalDate activationDate;
    private LocalDate expiryDate;

    private List<TransactionResponse> transactions;

}
