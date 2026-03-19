package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardBrand;
import az.bank.paymentsystem.enums.CardName;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
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
    private OrderStatus status;
    private String rejectionReason;
    private String cardHolderName;
    private CardName cardName;
    private CardBrand cardBrand;
    private CardType cardType;
    private Currency currency;
    private Instant processedAt;
    private Instant createdAt;
    private String password;
    //    private CustomerEntity customer;
    private Integer customerId;

}
