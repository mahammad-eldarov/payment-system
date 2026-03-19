package az.bank.paymentsystem.util.currentAccount;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.exception.AccountAlreadyCancelledException;
import az.bank.paymentsystem.exception.AccountExpiredException;
import az.bank.paymentsystem.exception.AccountLimitExceededException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.CustomerSuspiciousException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.OperationNotAllowedException;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.service.EntityFinderService;
import az.bank.paymentsystem.util.shared.CustomerSuspiciousValidator;
import az.bank.paymentsystem.util.shared.EnumParser;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountValidator {

    private final CustomerRepository customerRepository;
    private final CurrentAccountRepository currentAccountRepository;
//    private final EntityFinderService entityFinderService;
    private final CurrentAccountCreator currentAccountCreator;
    private final CustomerSuspiciousValidator suspiciousValidator;
    private final EnumParser enumParser;

//    public void validateRequestFields(OrderCurrentAccountRequest request) {
//        List<ExceptionResponse> errors = new ArrayList<>();
//
////        tryParse(() -> enumParser.parse(CardBrand.class, request.getCardBrand(), "cardBrand"), errors);
////        tryParse(() -> enumParser.parse(CardType.class, request.getCardType(), "cardType"), errors);
//        tryParse(() -> enumParser.parse(Currency.class, request.getCurrency(), "currency"), errors);
//
//        if (!errors.isEmpty()) {
//            throw new MultiValidationException(errors);
//        }
//    }
//
//    private void tryParse(Runnable parser, List<ExceptionResponse> errors) {
//        try {
//            parser.run();
//        } catch (IllegalArgumentException ex) {
//            errors.add(new ExceptionResponse(ex.getMessage()));
//        }
//    }

    public void validateCurrentAccountOrder(Integer customerId) {
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        List<ExceptionResponse> errors = new ArrayList<>();

        suspiciousValidator.validate(customer, errors);

        if (currentAccountRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 3) {
            errors.add(new ExceptionResponse(
                    422,
                    "The customer already has 3 current accounts. A new account cannot be ordered.",
                    LocalDateTime.now()
            ));
        }

        if (!errors.isEmpty()) {
            throw new MultiValidationException(errors);
        }
    }

//    public void process(CurrentAccountOrderEntity request) {
//        CustomerEntity customer = request.getCustomer();
//        List<ExceptionResponse> errors = new ArrayList<>();
//
//        suspiciousValidator.validate(customer, errors);
//
//        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
//            errors.add(new ExceptionResponse(
//                    403,
//                    "All your operations have been suspended due to suspicious activity.",
//                    LocalDateTime.now()
//            ));
//        }
//        if (currentAccountRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 3) {
//            errors.add(new ExceptionResponse(
//                    422,
//                    "The customer already has 3 current accounts. A new account cannot be ordered.",
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
//
//        CurrentAccountEntity account = currentAccountCreator.createOrderAccount(request);
//        currentAccountRepository.save(account);
//        request.setStatus(OrderStatus.APPROVED);
//        request.setUpdatedAt(Instant.now());
//    }

//    public void process(CurrentAccountOrderEntity request) {
//        List<String> reasons = new ArrayList<>();
//        CustomerEntity customer = request.getCustomer();
//
//        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
//            reasons.add("Customer is suspended due to suspicious activity.");
//        }
//        if (currentAccountRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 3) {
//            reasons.add("Current account limit exceeded.");
//        }
//
//        if (!reasons.isEmpty()) {
//            request.setStatus(OrderStatus.REJECTED);
//            request.setRejectionReason(String.join(", ", reasons));
//        } else {
//            CurrentAccountEntity account = currentAccountCreator.createOrderAccount(request);
//            currentAccountRepository.save(account);
//            request.setStatus(OrderStatus.APPROVED);
//            request.setUpdatedAt(Instant.now());
//        }
//    }

    public void validateDeletion(CurrentAccountEntity account) {
        if (account.getStatus() == CurrentAccountStatus.CLOSED) {
            throw new AccountAlreadyCancelledException("The current account has already been canceled.");
        }
        if (account.getStatus() == CurrentAccountStatus.EXPIRED) {
            throw new AccountExpiredException("An expired current account cannot be canceled.");
        }
        if (account.getStatus() == CurrentAccountStatus.SUSPICIOUS) {
            throw new OperationNotAllowedException("Cannot delete a suspicious current account. Please contact support.");
        }

    }

//    public void validateAccountOrder(Integer customerId, Integer accountCount) {
//
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
////        CustomerEntity customer = entityFinderService.findActiveCustomer(customerId);
//
//        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
//            throw new CustomerSuspiciousException("Your account is suspended due to suspicious activity.");
//        }
//
//        if (accountCount >= 3) {
//            throw new AccountLimitExceededException(
//                    "The customer already has 3 current accounts. A new account cannot be ordered.");
//        }
//    }
}
