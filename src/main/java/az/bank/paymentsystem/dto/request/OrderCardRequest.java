package az.bank.paymentsystem.dto.request;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
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

    @NotNull(message = "notNull.cardHolderName")
    private String cardHolderName;

    @NotNull(message = "notNull.cardName")
    private CardName cardName;

//    private OrderStatus status;
//
//    private String rejectionReason;

    @NotNull(message = "notNull.password")
    @Pattern(regexp = "^[0-9]{4}$", message = "pattern.password")
    private String password;

    @NotNull(message = "notNull.currency")
    private Currency currency;

    @NotNull(message = "notNull.cardBrand")
    private CardBrand cardBrand;

    @NotNull(message = "notNull.cardType")
    private CardType cardType;

//    private Instant updatedAt;
//
//    private CustomerEntity customer;


}
