package az.bank.paymentsystem.util.card;

import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.exception.CardAlreadyCancelledException;
import az.bank.paymentsystem.exception.CardExpiredException;
import az.bank.paymentsystem.exception.CardLimitExceededException;
import az.bank.paymentsystem.repository.CardRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardValidator {
    private final CardRepository cardRepository;

    public void validateCardOrder(Integer customerId) {

        boolean hasSuspiciousCard = cardRepository.existsByCustomerIdAndStatusIn(
                customerId, List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN));

        if (hasSuspiciousCard) {
            throw new CardLimitExceededException("Cannot order a new card while having suspicious, lost or stolen card. " +
                    "If you want to create a new card, you can close the cards that are in this status.");
        }

        int cardCount = cardRepository.countByCustomerIdAndIsVisibleTrue(customerId);
        if (cardCount >= 2) {
            throw new CardLimitExceededException("The customer already has 2 cards. A new card cannot be ordered.");
        }
    }

    public void validateCardDeletion(CardEntity card) {
        if (card.getStatus() == CardStatus.CLOSED) {
            throw new CardAlreadyCancelledException("The card has already been canceled.");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpiredException("An expired card cannot be canceled.");
        }
    }


}
