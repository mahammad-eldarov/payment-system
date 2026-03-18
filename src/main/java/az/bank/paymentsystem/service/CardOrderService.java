package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.mapper.CardOrderMapper;
import az.bank.paymentsystem.mapper.CurrentAccountOrderMapper;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CurrentAccountOrderRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.card.CardOrderProcessor;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardOrderService {

    private final CardOrderRepository cardOrderRequestRepository;
    private final CardOrderProcessor cardOrderRequestProcessor;
    private final CustomerRepository customerRepository;
//    private final EntityFinderService entityFinderService;
    private final CardOrderMapper cardOrderRequestMapper;

    public CardOrderResponse orderCard(Integer customerId, OrderCardRequest request) {
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        CardOrderEntity orderRequest = buildRequest(customer, request);
        cardOrderRequestRepository.save(orderRequest);
        cardOrderRequestProcessor.process(orderRequest);
        cardOrderRequestRepository.save(orderRequest);
        return cardOrderRequestMapper.toResponse(orderRequest);
    }

    private CardOrderEntity buildRequest(CustomerEntity customer, OrderCardRequest request) {
        CardOrderEntity orderRequest = new CardOrderEntity();
        orderRequest.setCustomer(customer);
        orderRequest.setStatus(OrderStatus.PENDING);
        orderRequest.setCardHolderName(request.getCardholderName());
        orderRequest.setCardName(request.getCardName());
        orderRequest.setCardBrand(request.getCardBrand());
//        orderRequest.setCardType(request.getCardType());
        orderRequest.setCurrency(request.getCurrency());
        orderRequest.setCreatedAt(Instant.now());
        return orderRequest;
    }
}