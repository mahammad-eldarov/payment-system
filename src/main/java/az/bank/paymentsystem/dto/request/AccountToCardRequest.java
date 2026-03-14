package az.bank.paymentsystem.dto.request;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountToCardRequest {

//    private PaymentSystem type;

    @NotNull(message = "Amount cannot be null.")
//    @DecimalMin(value = "0.01", message = "Amount must be greater than 0.")
    private BigDecimal amount;

//    @NotNull(message = "Currency cannot be null.")
//    private Currency currency;

    @NotBlank(message = "fromAccountNumber cannot be blank.")
    private String fromAccountNumber;

    @NotBlank(message = "toPan cannot be blank.")
    private String toPan;

}
