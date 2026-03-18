package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
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
    private OrderStatus status;
    private String rejectionReason;
    private String accountHolderName;
    private Currency currency;
    private Instant processedAt;
    private Instant createdAt;
}
