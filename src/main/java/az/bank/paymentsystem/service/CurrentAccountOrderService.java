package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.dto.response.CurrentAccountOrderResponse;
import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.mapper.CurrentAccountMapper;
import az.bank.paymentsystem.repository.CurrentAccountOrderRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.util.currentAccount.CurrentAccountOrderProcessor;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountCreator;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountOrderRejectionHandler;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountValidator;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentAccountOrderService {

    private final CurrentAccountOrderRepository currentAccountOrderRepository;
    private final CurrentAccountValidator currentAccountValidator;
    private final OrderRateLimitService  orderRateLimitService;
    private final CurrentAccountRepository currentAccountRepository;
    private final CurrentAccountCreator currentAccountCreator;
    private final CurrentAccountMapper currentAccountMapper;
    private final CustomerService customerService;
    private final CurrentAccountOrderRejectionHandler currentAccountOrderRejectionHandler;

    public CurrentAccountOrderResponse orderCurrentAccount(Integer customerId,
                                                           OrderCurrentAccountRequest request) {
        CustomerEntity customer = customerService.findActiveCustomer(customerId);

        orderRateLimitService.checkCooldown(customer, OrderType.CURRENT_ACCOUNT);

        CurrentAccountOrderEntity orderEntity = currentAccountCreator.createOrder(customer, request);

        try {
            currentAccountValidator.validateCurrentAccountOrder(customerId);
        } catch (MultiValidationException ex) {
            currentAccountOrderRejectionHandler.handleRejection(orderEntity, customer, ex);
            throw ex;
        }

        CurrentAccountEntity account = currentAccountCreator.createOrderAccount(request, customer);
        currentAccountRepository.save(account);

        orderEntity.setStatus(OrderStatus.APPROVED);
        orderEntity.setUpdatedAt(Instant.now());
        currentAccountOrderRepository.save(orderEntity);

        return currentAccountMapper.toOrderResponse(account);
    }

//    public CurrentAccountResponse orderCurrentAccount(Integer customerId,
//                                                      OrderCurrentAccountRequest request) {
//        CustomerEntity customer = findActiveCustomer(customerId);
//
//        orderRateLimitService.checkCooldown(customer, OrderType.CURRENT_ACCOUNT);
//
//        CurrentAccountOrderEntity orderEntity = buildOrderEntity(customer, request);
//
//        try {
//            currentAccountValidator.validateCurrentAccountOrder(customerId);
//        } catch (MultiValidationException ex) {
//            orderEntity.setStatus(OrderStatus.REJECTED);
//            orderEntity.setRejectionReason(
//                    ex.getErrors().stream()
//                            .map(ExceptionResponse::getMessage)
//                            .collect(Collectors.joining(", "))
//            );
//            orderRateLimitService.handleRejection(customer, OrderType.CURRENT_ACCOUNT);
//            currentAccountOrderRepository.save(orderEntity);
//            throw ex;
//        }
//
//        CurrentAccountEntity account = currentAccountCreator.createOrderAccount(request, customer);
//        currentAccountRepository.save(account);
//
//        orderEntity.setStatus(OrderStatus.APPROVED);
//        orderEntity.setUpdatedAt(Instant.now());
//        currentAccountOrderRepository.save(orderEntity);
//
//        return currentAccountMapper.toResponse(account);
//    }
//
//    private CurrentAccountOrderEntity buildOrderEntity(CustomerEntity customer,
//                                                       OrderCurrentAccountRequest request) {
//        CurrentAccountOrderEntity entity = new CurrentAccountOrderEntity();
//        entity.setCustomer(customer);
//        entity.setStatus(OrderStatus.PENDING);
//        entity.setCurrency(request.getCurrency());
//        entity.setCreatedAt(Instant.now());
//        return entity;
//    }
//
//    public CustomerEntity findActiveCustomer(Integer id) {
//        return customerRepository.findByIdAndIsVisibleTrue(id)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//    }
}