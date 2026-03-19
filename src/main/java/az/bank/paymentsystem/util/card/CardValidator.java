package az.bank.paymentsystem.util.card;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.exception.CardLimitExceededException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.CustomerSuspiciousException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.OperationNotAllowedException;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.service.EntityFinderService;
import az.bank.paymentsystem.util.shared.CustomerSuspiciousValidator;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.exception.CardAlreadyCancelledException;
import az.bank.paymentsystem.exception.CardExpiredException;
import az.bank.paymentsystem.repository.CardRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardValidator {
    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final CardCreator cardCreator;
    private final CustomerSuspiciousValidator suspiciousValidator;

//    private final EntityFinderService entityFinderService;

    //    public void process(CardOrderEntity request) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(
//                        request.getCustomer().getId())
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//
//        List<String> violations = collectViolations(customer);
//
//        if (!violations.isEmpty()) {
//            request.setStatus(OrderStatus.REJECTED);
//            request.setRejectionReason(String.join(", ", violations));
//            throw new CardOrderRejectedException(String.join(", ", violations));
//        }
//
//        CardEntity card = cardCreator.createOrderCard(request);
//        cardRepository.save(card);
//        request.setStatus(OrderStatus.APPROVED);
//        request.setUpdatedAt(Instant.now());
//    }
//public void process(CardOrderEntity request) {
//    CustomerEntity customer = request.getCustomer();
//    List<String> violations = collectViolations(customer);
//
//    if (!violations.isEmpty()) {
//        request.setStatus(OrderStatus.REJECTED);
//        request.setRejectionReason(String.join(", ", violations));
//        return; // ← exception yox, sadəcə qayıt
//    }
//
//    CardEntity card = cardCreator.createOrderCard(request);
//    cardRepository.save(card);
//    request.setStatus(OrderStatus.APPROVED);
//    request.setUpdatedAt(Instant.now());
//}OrderCardRequest request
//    public void validateCardOrder(Integer customerId) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//        List<ExceptionResponse> errors = new ArrayList<>();
//        suspiciousValidator.validate(customer, errors);
//
//        if (cardRepository.existsByCustomerIdAndStatusIn(customer.getId(),
//                List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN))) {
//            errors.add(new ExceptionResponse(
//                    403,
//                    "Cannot order a new card while having suspicious, lost or stolen card. " +
//                            "If you want to create a new card, you should close the cards that are in this status.",
//                    LocalDateTime.now()
//            ));
//        }
//        if (cardRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 2) {
//            errors.add(new ExceptionResponse(
//                    422,
//                    "The customer already has 2 cards. A new card cannot be ordered.",
//                    LocalDateTime.now()
//            ));
//        }
//
//        if (!errors.isEmpty()) {
//            request.setStatus(OrderStatus.REJECTED);
//            request.setRejectionReason(
//                    errors.stream().map(ExceptionResponse::getMessage).collect(Collectors.joining(", "))
//            );
//            throw new MultiValidationException(errors);
//        }
////CardEntity card = cardCreator.createOrderCard(request,customer);
//        CardEntity card = cardCreator.createOrderCard(request);
//        cardRepository.save(card);
//        request.setStatus(OrderStatus.APPROVED);
//        request.setUpdatedAt(Instant.now());
//    }

public void validateCardOrder(Integer customerId) {
    CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

    List<ExceptionResponse> errors = new ArrayList<>();

    suspiciousValidator.validate(customer, errors);

    if (cardRepository.existsByCustomerIdAndStatusIn(customer.getId(),
            List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN))) {
        errors.add(new ExceptionResponse(
                403,
                "Cannot order a new card while having suspicious, lost or stolen card. " +
                        "If you want to create a new card, you should close the cards that are in this status.",
                LocalDateTime.now()
        ));
    }
    if (cardRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 2) {
        errors.add(new ExceptionResponse(
                422,
                "The customer already has 2 cards. A new card cannot be ordered.",
                LocalDateTime.now()
        ));
    }

    if (!errors.isEmpty()) {
        throw new MultiValidationException(errors);
    }
}


//    public void validateCardOrder(Integer customerId) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
////        CustomerEntity customer = entityFinderService.findActiveCustomer(customerId);
//        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
//            throw new CustomerSuspiciousException("All your operations have been suspended due to suspicious activity.");
//        }
//
//        boolean hasSuspiciousCard = cardRepository.existsByCustomerIdAndStatusIn(
//                customerId, List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN));
////        Boolean hasSuspiciousCard = entityFinderService.findCustomerExistingCardStatus(customerId);
//
//        if (hasSuspiciousCard) {
//            throw new CardLimitExceededException("Cannot order a new card while having suspicious, lost or stolen card. " +
//                    "If you want to create a new card, you can close the cards that are in this status.");
//        }
//
//        Integer cardCount = cardRepository.countByCustomerIdAndIsVisibleTrue(customerId);
////        Integer cardCount = entityFinderService.countCustomerCardVisibleTrue(customerId);
//        if (cardCount >= 2) {
//            throw new CardLimitExceededException("The customer already has 2 cards. A new card cannot be ordered.");
//        }
//    }

    public void validateCardDeletion(CardEntity card) {
        if (card.getStatus() == CardStatus.CLOSED) {
            throw new CardAlreadyCancelledException("The card has already been canceled.");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpiredException("An expired card cannot be canceled.");
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            throw new OperationNotAllowedException("Cannot delete a suspicious card. Please contact support.");
        }
    }


}
