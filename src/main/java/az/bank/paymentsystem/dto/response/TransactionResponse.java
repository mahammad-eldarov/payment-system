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

//    private Integer paymentId;
//    private Integer customerId;
//    private BigDecimal amount;
//    private Currency currency;
//    private TransactionType transactionType;
//    private TransactionStatus status;
////    private PaymentSourceType fromType;
//    private Integer fromCardId;
//    private Integer fromAccountId;
////    private PaymentSourceType toType;
//    private Integer toCardId;
//    private Integer toAccountId;
//    private String description;
    private Integer paymentId;
    private String customerName;
    private BigDecimal amount;
    private Currency currency;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String paidBy;    // pan və ya accountNumber
    private String enrollTo;  // pan və ya accountNumber
    private String description;

}
