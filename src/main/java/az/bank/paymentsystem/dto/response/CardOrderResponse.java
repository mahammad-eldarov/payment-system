package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardBrand;
import az.bank.paymentsystem.enums.CardName;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardOrderResponse {
    private Integer id;
    private String cardholderName;
    private String cardName;
    private String pan;
    private String cvv;
    private String password;
    private CardBrand cardBrand;
    private CardType cardType;
    private BigDecimal balance;
    private Currency currency;
    private CardStatus status;
    private LocalDate activationDate;
    private LocalDate expiryDate;



}
