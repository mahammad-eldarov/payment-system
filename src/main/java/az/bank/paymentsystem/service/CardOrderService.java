package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.card.CardCreator;
import az.bank.paymentsystem.util.card.CardValidator;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardOrderService {

    private final CardOrderRepository cardOrderRepository;
    private final CustomerRepository customerRepository;
    private final CardValidator cardValidator;
    private final OrderRateLimitService orderRateLimitService;
    private final CardMapper cardMapper;
    private final CardCreator cardCreator;
    private final CardRepository cardRepository;


    public CardResponse orderCard(Integer customerId, OrderCardRequest request) {
        CustomerEntity customer = findActiveCustomer(customerId);

        orderRateLimitService.checkCooldown(customer, OrderType.CARD);

        CardOrderEntity orderEntity = buildOrderEntity(customer, request);

        try {
            cardValidator.validateCardOrder(customerId);
        } catch (MultiValidationException ex) {
            orderEntity.setStatus(OrderStatus.REJECTED);
            orderEntity.setRejectionReason(
                    ex.getErrors().stream()
                            .map(ExceptionResponse::getMessage)
                            .collect(Collectors.joining(", "))
            );
            orderRateLimitService.handleRejection(customer, OrderType.CARD);
            cardOrderRepository.save(orderEntity);
            throw ex;
        }

        CardEntity card = cardCreator.createCard(request, customer);
        cardRepository.save(card);

        orderEntity.setStatus(OrderStatus.APPROVED);
        orderEntity.setUpdatedAt(Instant.now());
        cardOrderRepository.save(orderEntity);

        CardResponse response = cardMapper.toResponse(card);
        response.setCvv(card.getCvv());
        response.setPassword(request.getPassword());
        return response;
    }

    private CardOrderEntity buildOrderEntity(CustomerEntity customer, OrderCardRequest request) {
        CardOrderEntity entity = new CardOrderEntity();
        entity.setCustomer(customer);
        entity.setStatus(OrderStatus.PENDING);
        entity.setCardHolderName(request.getCardHolderName());
        entity.setCardName(request.getCardName());
        entity.setCardBrand(request.getCardBrand());
        entity.setCardType(request.getCardType());
        entity.setPassword(request.getPassword());
        entity.setCurrency(request.getCurrency());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    public CustomerEntity findActiveCustomer(Integer id) {
        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }
}