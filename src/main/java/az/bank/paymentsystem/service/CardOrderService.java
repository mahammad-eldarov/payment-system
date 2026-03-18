package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.exception.CardOrderRejectedException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CardOrderMapper;
import az.bank.paymentsystem.mapper.CurrentAccountOrderMapper;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CurrentAccountOrderRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.util.card.CardOrderProcessor;
import az.bank.paymentsystem.util.card.CardValidator;
import java.time.Instant;
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

    public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        CardOrderEntity orderRequest = buildRequest(customer, request);

        try {
            cardValidator.process(orderRequest);
        } catch (MultiValidationException ex) {
            cardOrderRepository.save(orderRequest); // REJECTED kimi saxla
            throw ex;                               // yenidən at → GlobalExceptionHandler tutur
        }

        cardOrderRepository.save(orderRequest); // APPROVED kimi saxla
        return cardOrderMapper.toResponse(orderRequest);
    }

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
////        entityFinderService.saveCard(card);
//
//        CardResponse response = cardMapper.toResponse(card);
//        response.setCvv(card.getCvv());
//        response.setPassword(request.getPassword());
//
//        return response;
//    }



    private CardOrderEntity buildRequest(CustomerEntity customer, OrderCardRequest request) {
        CardOrderEntity orderRequest = new CardOrderEntity();
        orderRequest.setCustomer(customer);
        orderRequest.setStatus(OrderStatus.PENDING);
        orderRequest.setCardHolderName(request.getCardholderName());
        orderRequest.setCardName(request.getCardName());
        orderRequest.setCardBrand(request.getCardBrand());
        orderRequest.setCardType(request.getCardType());
        orderRequest.setPassword(request.getPassword());
        orderRequest.setCurrency(request.getCurrency());
        orderRequest.setCreatedAt(Instant.now());
        return orderRequest;
    }
}