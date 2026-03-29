package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.TransactionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Integer id;
    private Integer paymentId;
    private String customerName;
    private BigDecimal amount;
    private Currency currency;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String paidBy;
    private String enrollTo;
    private String description;

}
