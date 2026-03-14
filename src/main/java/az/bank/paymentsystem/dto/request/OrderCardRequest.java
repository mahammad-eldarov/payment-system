package az.bank.paymentsystem.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.CardBrand;
import az.bank.paymentsystem.enums.CardName;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCardRequest {

//    @NotNull(message = "Customer ID cannot be empty.")
//    private Integer customerId;

    @NotNull(message = "Cardholder name cannot be empty.")
    private String cardholderName;

    @NotNull(message = "Card name cannot be empty.")
    private CardName cardName;

    @NotNull(message = "Password cannot be empty.")
    @Pattern(regexp = "^[0-9]{4}$", message = "Password must be exactly 4 digits")
    private String password;

    @NotNull(message = "Currency cannot be empty.")
    private Currency currency;

    @NotNull(message = "Card brand cannot be empty.")
    private CardBrand cardBrand;

    @NotNull(message = "Card type cannot be empty.")
    private CardType cardType;


}
