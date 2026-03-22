package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.enums.CardBrand;
import az.bank.paymentsystem.enums.CardName;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardForCustomerResponse {
    private String cardholderName;
    private CardName cardName;
    private String pan;
    private String cvv;
    private CardBrand cardBrand;
    private CardType cardType;
    private BigDecimal balance;
    private Currency currency;
    private CardStatus status;
    private LocalDate expiryDate;
}
