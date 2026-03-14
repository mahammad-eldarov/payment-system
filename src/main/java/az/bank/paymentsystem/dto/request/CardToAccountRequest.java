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
public class CardToAccountRequest {


//    private PaymentSystem type;

    @NotNull(message = "Amount cannot be null.")
//    @DecimalMin(value = "0.01", message = "Amount must be greater than 0.")
    private BigDecimal amount;

//    @NotNull(message = "Currency cannot be null.")
//    private Currency currency;

    @NotBlank(message = "fromPan cannot be blank.")
    private String fromPan;

    @NotBlank(message = "toAccountNumber cannot be blank.")
    private String toAccountNumber;

}
