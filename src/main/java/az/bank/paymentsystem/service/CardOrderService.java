package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CardOrderRejectedException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.mapper.CardOrderMapper;
import az.bank.paymentsystem.mapper.CurrentAccountOrderMapper;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountOrderRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.util.card.CardOrderProcessor;
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
    //    private final CardOrderProcessor cardOrderProcessor;
    private final CustomerRepository customerRepository;
    private final CardValidator cardValidator;
    //    private final EntityFinderService entityFinderService;
    private final CardOrderMapper cardOrderMapper;
    private final OrderRateLimitService orderRateLimitService;
    private final CardMapper cardMapper;
    private final CardCreator cardCreator;
    private final CardRepository cardRepository;


//        cardValidator.validateCardOrder(customerId);
//
//        CardEntity card = cardCreator.createCard(request, customer);


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

//        public CardResponse orderCard(Integer customerId, OrderCardRequest request) {
//        CustomerEntity customer = findActiveCustomer(customerId);
//        cardValidator.validateCardOrder(customerId);
//
//        CardEntity card = cardCreator.createCard(request, customer);
//        cardRepository.save(card);
////        entityFinderService.saveCard(card);
//
//        CardResponse response = cardMapper.toResponse(card);
//        response.setCvv(card.getCvv());
//        response.setPassword(request.getPassword());
//
//        return response;
//    }
//
//    public CustomerEntity findActiveCustomer(Integer id) {
//        return customerRepository.findByIdAndIsVisibleTrue(id)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//    }

//    public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//
//        orderRateLimitService.checkCooldown(customer, OrderType.CARD); // ← yoxla
//
////        CardOrderEntity orderRequest = buildRequest(customer, request);
//        OrderCardRequest orderRequest = buildRequest(request);
//        try {
//            cardValidator.process(orderRequest);
//        } catch (MultiValidationException ex) {
//            orderRateLimitService.handleRejection(customer, OrderType.CARD); // ← say
//            cardOrderRepository.save(orderRequest);
//            throw ex;
//        }
//
//        orderRateLimitService.resetLimit(customer, OrderType.CARD); // ← sıfırla
//        cardOrderRepository.save(orderRequest);
//        return cardOrderMapper.toResponse(orderRequest);
//    }

// ən sonuncu budur
//    public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//
//        orderRateLimitService.checkCooldown(customer, OrderType.CARD);
//
//        CardOrderEntity orderEntity = buildRequest(customer, request);
//
//        try {
//            cardValidator.process(customerId, request);
//        } catch (MultiValidationException ex) {
//            orderEntity.setStatus(OrderStatus.REJECTED);
//            orderEntity.setRejectionReason(
//                    ex.getErrors().stream()
//                            .map(ExceptionResponse::getMessage)
//                            .collect(Collectors.joining(", "))
//            );
//            orderRateLimitService.handleRejection(customer, OrderType.CARD);
//            cardOrderRepository.save(orderEntity);
//            throw ex;
//        }
//
//        orderEntity.setStatus(OrderStatus.APPROVED);
//        orderEntity.setUpdatedAt(Instant.now());
//        orderRateLimitService.resetLimit(customer, OrderType.CARD);
//        cardOrderRepository.save(orderEntity);
//        return cardOrderMapper.toResponse(orderEntity);
//    }

//    public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//
//        CardOrderEntity orderRequest = buildRequest(customer, request);
//
//        try {
//            cardValidator.process(orderRequest);
//        } catch (MultiValidationException ex) {
//            cardOrderRepository.save(orderRequest); // REJECTED kimi saxla
//            throw ex;                               // yenidən at → GlobalExceptionHandler tutur
//        }
//
//        cardOrderRepository.save(orderRequest); // APPROVED kimi saxla
//        return cardOrderMapper.toResponse(orderRequest);
//    }

//    public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//        CardOrderEntity orderRequest = buildRequest(customer, request);
//        cardOrderRepository.save(orderRequest);
//        cardValidator.process(orderRequest);
//        cardOrderRepository.save(orderRequest);
//        return cardOrderMapper.toResponse(orderRequest);
//    }
//public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
//    CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//
//    CardOrderEntity orderRequest = buildRequest(customer, request);
//    cardValidator.process(orderRequest);         // statusu qur
//    cardOrderRepository.save(orderRequest);      // ← bir dəfə, final status ilə saxla
//
//    if (orderRequest.getStatus() == OrderStatus.REJECTED) {
//        throw new CardOrderRejectedException(orderRequest.getRejectionReason());
//    }
//
//    return cardOrderMapper.toResponse(orderRequest);
//}

//    public CardResponse orderCard(Integer customerId, OrderCardRequest request) {
//        CustomerEntity customer = findActiveCustomer(customerId);
//        cardValidator.validateCardOrder(customerId);
//
//        CardEntity card = cardCreator.createCard(request, customer);
//        cardRepository.save(card);

    /// /        entityFinderService.saveCard(card);
//
//        CardResponse response = cardMapper.toResponse(card);
//        response.setCvv(card.getCvv());
//        response.setPassword(request.getPassword());
//
//        return response;
//    }


//    private CardOrderResponse buildRequest(CustomerEntity customer, OrderCardRequest request) {
//        CardOrderEntity orderRequest = new CardOrderEntity();
//        orderRequest.setCustomer(customer);
//        orderRequest.setStatus(OrderStatus.PENDING);
//        orderRequest.setCardHolderName(request.getCardHolderName());
//        orderRequest.setCardName(request.getCardName());
//        orderRequest.setCardBrand(request.getCardBrand());
//        orderRequest.setCardType(request.getCardType());
//        orderRequest.setPassword(request.getPassword());
//        orderRequest.setCurrency(request.getCurrency());
//        orderRequest.setCreatedAt(Instant.now());
//        return orderRequest;
//    }
//    private CardOrderEntity buildRequest(CustomerEntity customer, OrderCardRequest request) {
//        CardOrderEntity entity = new CardOrderEntity();
//        entity.setCustomer(customer);
//        entity.setStatus(OrderStatus.PENDING);
//        entity.setCardHolderName(request.getCardHolderName());
//        entity.setCardName(request.getCardName());
//        entity.setCardBrand(request.getCardBrand());
//        entity.setCardType(request.getCardType());
//        entity.setPassword(request.getPassword());
//        entity.setCurrency(request.getCurrency());
//        entity.setCreatedAt(Instant.now());
//        return entity;
//    }
}