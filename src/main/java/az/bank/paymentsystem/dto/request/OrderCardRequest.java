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

    @NotNull(message = "{notNull.cardHolderName}")
    private String cardHolderName;

    @NotNull(message = "{notNull.cardName}")
    private CardName cardName;

    @NotNull(message = "{notNull.password}")
    @Pattern(regexp = "^[0-9]{4}$", message = "{pattern.password}")
    private String password;

    @NotNull(message = "{notNull.currency}")
    private Currency currency;

    @NotNull(message = "{notNull.cardBrand}")
    private CardBrand cardBrand;

    @NotNull(message = "{notNull.cardType}")
    private CardType cardType;


}
