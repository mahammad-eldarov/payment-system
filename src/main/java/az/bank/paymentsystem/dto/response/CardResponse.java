package az.bank.paymentsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.CardBrand;
import az.bank.paymentsystem.enums.CardName;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardResponse {

    private Integer id;
    private String cardholderName;
    private CardName cardName;
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


    private List<TransactionResponse> transactions;

}
