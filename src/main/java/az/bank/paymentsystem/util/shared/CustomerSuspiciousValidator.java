package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.dto.request.CreateCustomerRequest;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.repository.StatusAuditLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerSuspiciousValidator {

    private final StatusAuditLogRepository statusAuditLogRepository;
    private final MessageSource messageSource;

    public void validate(CustomerEntity customer, List<ExceptionResponse> errors) {
        Locale locale = LocaleContextHolder.getLocale();

        int suspiciousCount = statusAuditLogRepository
                .countByEntityTypeAndEntityIdAndNewStatus(
                        "CUSTOMER",
                        customer.getId(),
                        "SUSPICIOUS"
                );

        if (suspiciousCount >= 1) {
            errors.add(new ExceptionResponse(
                    403,
                    messageSource.getMessage("customerSuspiciousValidator.validate.permanentlyBlocked",null,locale),
                    LocalDateTime.now()
            ));
        }

    }
}