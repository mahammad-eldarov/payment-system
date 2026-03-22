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
//import az.bank.paymentsystem.util.shared.EnumParser;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountValidator {

    private final CustomerRepository customerRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerSuspiciousValidator suspiciousValidator;
    private final MessageSource messageSource;


    public void validateCurrentAccountOrder(Integer customerId) {
        Locale locale =  LocaleContextHolder.getLocale();
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("currentAccountValidator.validateCurrentAccountOrder.customerNotFound", null, locale)));

        List<ExceptionResponse> errors = new ArrayList<>();

        suspiciousValidator.validate(customer, errors);

        if (currentAccountRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 3) {
            errors.add(new ExceptionResponse(
                    422,
                    messageSource.getMessage("currentAccountValidator.validateCurrentAccountOrder.hasThreeAccount", null, locale),
                    LocalDateTime.now()
            ));
        }

        if (!errors.isEmpty()) {
            throw new MultiValidationException(errors);
        }
    }

    public void validateDeletion(CurrentAccountEntity account) {
        Locale locale =  LocaleContextHolder.getLocale();

        if (account.getStatus() == CurrentAccountStatus.CLOSED) {
            throw new AccountAlreadyCancelledException(messageSource.getMessage("currentAccountValidator.validateDeletion.accountAlreadyCanceled", null, locale));
        }
        if (account.getStatus() == CurrentAccountStatus.EXPIRED) {
            throw new AccountExpiredException(messageSource.getMessage("currentAccountValidator.validateDeletion.expiryAccountCanceled", null, locale));
        }
        if (account.getStatus() == CurrentAccountStatus.SUSPICIOUS) {
            throw new OperationNotAllowedException(messageSource.getMessage("currentAccountValidator.validateDeletion.suspiciousAccountCanNotDeleted", null, locale));
        }

    }
}