package az.bank.paymentsystem.util.tin;

import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.exception.TinAlreadyCancelledException;
import az.bank.paymentsystem.exception.TinExpiredException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.OperationNotAllowedException;
import az.bank.paymentsystem.repository.TinRepository;
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
public class TinValidator {

    private final CustomerRepository customerRepository;
    private final TinRepository tinRepository;
    private final CustomerSuspiciousValidator suspiciousValidator;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;


    public void validateTinOrder(Integer customerId) {
        Locale fallbackLocale = LocaleContextHolder.getLocale();

        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("tinValidator.validateTinOrder.customerNotFound", null, fallbackLocale)));

        Locale locale = messageUtil.resolveLocale(customer);

        List<ExceptionResponse> errors = new ArrayList<>();

        suspiciousValidator.validate(customer, errors);

        if (tinRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 3) {
            errors.add(new ExceptionResponse(
                    422,
                    messageSource.getMessage("tinValidator.validateTinOrder.hasThreeTin", null, locale),
                    LocalDateTime.now()
            ));
        }

        if (!errors.isEmpty()) {
            throw new MultiValidationException(errors);
        }
    }

    public void validateDeletion(TinEntity tin) {
        Locale locale = messageUtil.resolveLocale(tin.getCustomer());


        if (tin.getStatus() == TinStatus.CLOSED) {
            throw new TinAlreadyCancelledException(messageSource.getMessage("tinValidator.validateDeletion.tinAlreadyCanceled", null, locale));
        }
        if (tin.getStatus() == TinStatus.EXPIRED) {
            throw new TinExpiredException(messageSource.getMessage("tinValidator.validateDeletion.expiryTinCanceled", null, locale));
        }
        if (tin.getStatus() == TinStatus.SUSPICIOUS) {
            throw new OperationNotAllowedException(messageSource.getMessage("tinValidator.validateDeletion.suspiciousTinCanNotDeleted", null, locale));
        }

    }
}