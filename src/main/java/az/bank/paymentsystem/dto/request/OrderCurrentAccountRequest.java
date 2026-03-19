package az.bank.paymentsystem.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCurrentAccountRequest {

//    @NotNull(message = "Customer ID cannot be empty.")
//    private Integer customerId;

    @NotNull(message = "Currency cannot be empty.")
    private String currency;

    @NotNull(message = "Current account holder name cannot be empty.")
    private String currentAccountHolderName;


}
