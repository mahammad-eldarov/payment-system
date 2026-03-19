package az.bank.paymentsystem.util.card;

import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.util.shared.LuhnPanGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardCreator {
    private final LuhnPanGenerator luhnPanGenerator;
    private final CvvGenerator cvvGenerator;

    public CardEntity createCard(OrderCardRequest request, CustomerEntity customer) {
        CardEntity card = new CardEntity();
        card.setCardholderName(request.getCardHolderName());
        card.setCardName(request.getCardName());
        card.setCardBrand(request.getCardBrand());
        card.setCardType(request.getCardType());
        card.setPan(luhnPanGenerator.generate());
        card.setCvv(cvvGenerator.generate());
        card.setPassword(request.getPassword());
        card.setBalance(BigDecimal.ZERO);
        card.setCurrency(request.getCurrency());
        card.setStatus(CardStatus.ACTIVE);
        card.setActivationDate(LocalDate.now());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setCustomer(customer);
        card.setIsVisible(true);
        card.setCreatedAt(Instant.now());
        return card;
    }

}
