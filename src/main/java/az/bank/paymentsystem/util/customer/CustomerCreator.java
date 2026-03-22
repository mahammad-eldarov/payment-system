package az.bank.paymentsystem.util.customer;

//import az.bank.paymentsystem.service.EntityFinderService;
import az.bank.paymentsystem.util.shared.FraudBlacklistChecker;
import az.bank.paymentsystem.util.shared.FraudDetectionChecker;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.CreateCustomerRequest;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.CustomerPinAlreadyExistsException;
import az.bank.paymentsystem.repository.CustomerRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerCreator {

    private final CustomerRepository customerRepository;
    private final FraudBlacklistChecker fraudBlacklistChecker;
    private final FraudDetectionChecker fraudDetectionChecker;
    private final MessageSource messageSource;


    public CustomerEntity createCustomer(CreateCustomerRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        fraudBlacklistChecker.checkBlacklist(
                request.getPin(), request.getPhoneNumber(), request.getEmail());
        fraudDetectionChecker.checkDeletedSuspiciousCustomer(request.getPin());
        fraudDetectionChecker.checkAccountCreationFrequency(request.getPin());

        if (customerRepository.existsByPinAndIsVisibleTrue(request.getPin())) {
            throw new CustomerPinAlreadyExistsException(messageSource.getMessage("customerCreator.createCustomer.pinAvailable", null, locale));
        }

        CustomerEntity customer = new CustomerEntity();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setPin(request.getPin());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setIsVisible(true);
        customer.setCreatedAt(Instant.now());
        return customer;
    }
}
