package az.bank.paymentsystem.util.currentAccount;

import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.exception.AccountAlreadyCancelledException;
import az.bank.paymentsystem.exception.AccountExpiredException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.OperationNotAllowedException;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.shared.CustomerSuspiciousValidator;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private final MessageUtil messageUtil;


    public void validateCurrentAccountOrder(Integer customerId) {
        Locale fallbackLocale = LocaleContextHolder.getLocale();

        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("currentAccountValidator.validateCurrentAccountOrder.customerNotFound", null, fallbackLocale)));

        Locale locale = messageUtil.resolveLocale(customer);

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
        Locale locale = messageUtil.resolveLocale(account.getCustomer());


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