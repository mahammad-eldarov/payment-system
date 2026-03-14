package az.bank.paymentsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private BigDecimal amount;
    private Currency currency;
    private PaymentStatus status;
    private LocalDate scheduledDate;
//    private Instant processedAt;
//    private String failureReason;
//    private PaymentSourceType fromType;
//    private Integer fromCardId;
//    private Integer fromAccountId;
//    private PaymentSourceType toType;
//    private Integer toCardId;
//    private Integer toAccountId;

}
