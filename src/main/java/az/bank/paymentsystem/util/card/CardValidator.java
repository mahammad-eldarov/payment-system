package az.bank.paymentsystem.util.card;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.CustomerSuspiciousException;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.service.EntityFinderService;
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
    private final CustomerRepository customerRepository;
//    private final EntityFinderService entityFinderService;

    public void validateCardOrder(Integer customerId) {
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//        CustomerEntity customer = entityFinderService.findActiveCustomer(customerId);
        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
            throw new CustomerSuspiciousException("Your account is suspended due to suspicious activity.");
        }

        boolean hasSuspiciousCard = cardRepository.existsByCustomerIdAndStatusIn(
                customerId, List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN));
//        Boolean hasSuspiciousCard = entityFinderService.findCustomerExistingCardStatus(customerId);

        if (hasSuspiciousCard) {
            throw new CardLimitExceededException("Cannot order a new card while having suspicious, lost or stolen card. " +
                    "If you want to create a new card, you can close the cards that are in this status.");
        }

        Integer cardCount = cardRepository.countByCustomerIdAndIsVisibleTrue(customerId);
//        Integer cardCount = entityFinderService.countCustomerCardVisibleTrue(customerId);
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
