package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.util.card.CardCreator;
import az.bank.paymentsystem.util.card.CardOrderRejectionHandler;
import az.bank.paymentsystem.util.card.CardValidator;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardOrderService {

    private final CardOrderRepository cardOrderRepository;
    private final CustomerService customerService;
    private final CardValidator cardValidator;
    private final OrderRateLimitService orderRateLimitService;
    private final CardMapper cardMapper;
    private final CardCreator cardCreator;
    private final CardRepository cardRepository;
    private final CardOrderRejectionHandler cardOrderRejectionHandler;

    public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
        CustomerEntity customer = customerService.findActiveCustomer(customerId);

        orderRateLimitService.checkCooldown(customer, OrderType.CARD);

        CardOrderEntity orderEntity = cardCreator.createOrder(customer, request);

        try {
            cardValidator.validateCardOrder(customerId);
        } catch (MultiValidationException ex) {
            cardOrderRejectionHandler.handleRejection(orderEntity, customer, ex);
            throw ex;
        }

        CardEntity card = cardCreator.createCard(request, customer);
        cardRepository.save(card);

        orderEntity.setStatus(OrderStatus.APPROVED);
        orderEntity.setUpdatedAt(Instant.now());
        cardOrderRepository.save(orderEntity);

        CardOrderResponse response = cardMapper.toOrderResponse(card);
        response.setCvv(card.getCvv());
        response.setPassword(request.getPassword());
        return response;
    }
}