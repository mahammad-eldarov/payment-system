package az.bank.paymentsystem.util;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.CreateCustomerRequest;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.CustomerPinAlreadyExistsException;
import az.bank.paymentsystem.repository.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerCreator {

    private final CustomerRepository customerRepository;

    public CustomerEntity createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByPinAndIsVisibleTrue(request.getPin())) {
            throw new CustomerPinAlreadyExistsException("This pin is already available.");
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
