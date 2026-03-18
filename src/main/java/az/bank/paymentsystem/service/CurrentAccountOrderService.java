package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.dto.response.CurrentAccountOrderResponse;
import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CurrentAccountOrderMapper;
import az.bank.paymentsystem.repository.CurrentAccountOrderRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.util.currentAccount.CurrentAccountOrderProcessor;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountValidator;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentAccountOrderService {

    private final CurrentAccountOrderRepository currentAccountOrderRepository;
//    private final CurrentAccountOrderProcessor currentAccountOrderRequestProcessor;
    private final CustomerRepository customerRepository;
//    private final EntityFinderService entityFinderService;
    private final CurrentAccountOrderMapper currentAccountOrderMapper;
    private final CurrentAccountValidator currentAccountValidator;
    private final OrderRateLimitService  orderRateLimitService;

//    public CurrentAccountOrderResponse orderAccount(Integer customerId, OrderCurrentAccountRequest request) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//        CurrentAccountOrderEntity orderRequest = buildRequest(customer, request);
//        currentAccountOrderRequestRepository.save(orderRequest);
//        currentAccountValidator.process(orderRequest);
//        currentAccountOrderRequestRepository.save(orderRequest);
//        return currentAccountOrderRequestMapper.toResponse(orderRequest);
//    }

    public CurrentAccountOrderResponse orderAccount(Integer customerId, OrderCurrentAccountRequest request) {
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        orderRateLimitService.checkCooldown(customer, OrderType.CURRENT_ACCOUNT);

        CurrentAccountOrderEntity orderRequest = buildRequest(customer, request);

        try {
            currentAccountValidator.process(orderRequest);
        } catch (MultiValidationException ex) {
            orderRateLimitService.handleRejection(customer, OrderType.CURRENT_ACCOUNT);
            currentAccountOrderRepository.save(orderRequest);
            throw ex;
        }

        orderRateLimitService.resetLimit(customer, OrderType.CURRENT_ACCOUNT);
        currentAccountOrderRepository.save(orderRequest);
        return currentAccountOrderMapper.toResponse(orderRequest);
    }

    private CurrentAccountOrderEntity buildRequest(CustomerEntity customer, OrderCurrentAccountRequest request) {
        CurrentAccountOrderEntity orderRequest = new CurrentAccountOrderEntity();
        orderRequest.setCustomer(customer);
        orderRequest.setStatus(OrderStatus.PENDING);
        orderRequest.setAccountHolderName(request.getCurrentAccountHolderName());
        orderRequest.setCurrency(request.getCurrency());
        orderRequest.setCreatedAt(Instant.now());
        return orderRequest;
    }
}