package az.bank.paymentsystem.dto.request;
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
public class AccountToAccountRequest {

    @NotNull(message = "{notNull.bigDecimal}")
    private BigDecimal amount;

    @NotBlank(message = "{notBlank.fromAccountNumber}")
    private String fromAccountNumber;

    @NotBlank(message = "{notBlank.toAccountNumber}")
    private String toAccountNumber;


}
