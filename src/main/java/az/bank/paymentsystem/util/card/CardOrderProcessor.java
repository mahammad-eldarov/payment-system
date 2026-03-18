package az.bank.paymentsystem.util.card;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardOrderProcessor {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final CardCreator cardCreator;

    public void process(CardOrderEntity request) {
        List<String> reasons = new ArrayList<>();
        CustomerEntity customer = request.getCustomer();

        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
            reasons.add("Customer is suspended due to suspicious activity.");
        }
        if (cardRepository.existsByCustomerIdAndStatusIn(customer.getId(),
                List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN))) {
            reasons.add("Customer has suspicious, lost or stolen card.");
        }
        if (cardRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 2) {
            reasons.add("Card limit exceeded.");
        }

        if (!reasons.isEmpty()) {
            request.setStatus(OrderStatus.REJECTED);
            request.setRejectionReason(String.join(", ", reasons));
        } else {
            CardEntity card = cardCreator.createOrderCard(request);
            cardRepository.save(card);
            request.setStatus(OrderStatus.APPROVED);
            request.setUpdatedAt(Instant.now());
        }
    }
}