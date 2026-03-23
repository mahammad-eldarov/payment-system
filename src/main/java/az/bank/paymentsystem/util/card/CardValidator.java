package az.bank.paymentsystem.util.card;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.OperationNotAllowedException;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.shared.CustomerSuspiciousValidator;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.exception.CardAlreadyCancelledException;
import az.bank.paymentsystem.exception.CardExpiredException;
import az.bank.paymentsystem.repository.CardRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardValidator {
    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final CustomerSuspiciousValidator suspiciousValidator;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public void validateCardOrder(Integer customerId) {
        Locale fallbackLocale = LocaleContextHolder.getLocale();
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("cardValidator.validateCardOrder.customerNotFound",null,fallbackLocale)));

        Locale locale = messageUtil.resolveLocale(customer);
        List<ExceptionResponse> errors = new ArrayList<>();

        suspiciousValidator.validate(customer, errors);

        if (cardRepository.existsByCustomerIdAndStatusIn(customer.getId(),
                List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN))) {
            errors.add(new ExceptionResponse(
                    403,
                    messageSource.getMessage("cardValidator.validateCardOrder.canNotOrder",null,locale),
                    LocalDateTime.now()
            ));
        }
        if (cardRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 2) {
            errors.add(new ExceptionResponse(
                    422,
                    messageSource.getMessage("cardValidator.validateCardOrder.hasTwoCard",null,locale),
                    LocalDateTime.now()
            ));
        }

        if (!errors.isEmpty()) {
            throw new MultiValidationException(errors);
        }
    }


    public void validateCardDeletion(CardEntity card) {
        Locale locale = card.getCustomer() != null && card.getCustomer().getLanguage() != null
                ? card.getCustomer().getLanguage().toLocale()
                : Language.AZ.toLocale();
        if (card.getStatus() == CardStatus.CLOSED) {
            throw new CardAlreadyCancelledException(messageSource.getMessage("cardValidator.validateCardDeletion.cardAlreadyCanceled", null, locale));
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpiredException(messageSource.getMessage("cardValidator.validateCardDeletion.expiredCardCanceled", null, locale));
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            throw new OperationNotAllowedException(messageSource.getMessage("cardValidator.validateCardDeletion.canNotDeleteSuspiciousCard", null, locale));
        }
    }


}
