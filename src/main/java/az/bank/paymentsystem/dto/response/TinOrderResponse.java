package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.TinStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TinOrderResponse {
    private Integer id;
    private String tinNumber;
    private String tinHolderName;
    private BigDecimal balance;
    private Currency currency;
    private TinStatus status;
    private LocalDate activationDate;
    private LocalDate expiryDate;
}
