package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.CustomerShortResponse;
import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.repository.CustomerRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.exception.CustomerDeletedException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.dto.request.CreateCustomerRequest;
import az.bank.paymentsystem.dto.request.UpdateCustomerRequest;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.mapper.CustomerMapper;
import az.bank.paymentsystem.util.customer.CustomerCreator;
import az.bank.paymentsystem.util.customer.CustomerResponseBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerResponseBuilder customerResponseBuilder;
    private final CustomerCreator customerCreator;
    private final MessageSource messageSource;

    public CustomerShortResponse createCustomer(CreateCustomerRequest request) {
        CustomerEntity customer = customerCreator.createCustomer(request);
        customerRepository.save(customer);


        CustomerShortResponse response = customerMapper.toShortResponse(customer);
        response.setPin(request.getPin());
        return response;
    }

    public CustomerShortResponse getCustomerById(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        Optional<CustomerEntity> activeCustomer = customerRepository.findByIdAndIsVisibleTrue(id);
        if (activeCustomer.isPresent()) {
            return customerMapper.toShortResponse(activeCustomer.get());
        }
        if (customerRepository.findByIdAndIsVisibleFalse(id).isPresent()) {
            throw new CustomerDeletedException(messageSource.getMessage("customerService.getCustomerById.customerDeleted", null, locale));
        }
        throw new CustomerNotFoundException(messageSource.getMessage("customerService.getCustomerById.customerNotFound",null, locale));
    }

    public List<CustomerShortResponse> getCustomersByStatus(CustomerStatus status) {
        Locale locale = LocaleContextHolder.getLocale();

        List<CustomerEntity> activeCustomers = customerRepository.findByStatusAndIsVisibleTrue(status);

        if (!activeCustomers.isEmpty()) {
            return activeCustomers.stream().map(customerMapper::toShortResponse).collect(Collectors.toList());
        }
        if (!customerRepository.findByStatusAndIsVisibleFalse(status).isEmpty()) {
            throw new CustomerDeletedException(messageSource.getMessage("customerService.getCustomersByStatus.customerDeletedStatus", null, locale));
        }
        throw new CustomerNotFoundException(messageSource.getMessage("customerService.getCustomersByStatus.customerNotFoundStatus", null, locale));
    }

    public CustomerResponse getDeletedCustomerById(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleFalse(id).orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("customerService.getDeletedCustomerById.customerNotFound", null, locale)));
        CustomerResponse response = customerMapper.toResponse(customer);
        customerResponseBuilder.setCardsAndAccounts(response, id);
        return response;
    }

    public List<CustomerShortResponse> getAllCustomers() {
        Locale locale = LocaleContextHolder.getLocale();

        List<CustomerEntity> activeCustomer = customerRepository.findAllByIsVisibleTrue();

        if (activeCustomer.isEmpty()) {
            throw new EmptyListException(messageSource.getMessage("customerService.getAllCustomers.listEmpty", null, locale));
        }

        return activeCustomer.stream().map(customerMapper::toShortResponse).collect(Collectors.toList());
    }

    public CustomerResponse getCustomersCardsAndAccounts(Integer id) {
        CustomerResponse response = customerMapper.toResponse(findActiveCustomer(id));
        customerResponseBuilder.setCardsAndAccounts(response, id);
        return response;
    }

    public Page<TransactionResponse> getCardTransactions(Integer customerId, String pan, int page) {
        findActiveCustomer(customerId);
        return customerResponseBuilder.buildCardTransactions(customerId, pan, page);
    }


    public Page<TransactionResponse> getAccountTransactions(Integer customerId, String accountNumber, int page) {
        findActiveCustomer(customerId);
        return customerResponseBuilder.buildAccountTransactions(customerId, accountNumber, page);
    }

    public MessageResponse updateCustomerStatus(Integer id, CustomerStatus status) {
        Locale locale = LocaleContextHolder.getLocale();

        CustomerEntity customer = findActiveCustomer(id);
        customer.setStatus(status);
        customer.setUpdatedAt(Instant.now());
        customerRepository.save(customer);
        return new MessageResponse(messageSource.getMessage("customerService.updateCustomerStatus.statusUpdated", null, locale));
    }

    public MessageResponse updateCustomer(Integer id, UpdateCustomerRequest request) {
        Locale locale = LocaleContextHolder.getLocale();


        CustomerEntity customer = findActiveCustomer(id);

        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getSurname() != null) {
            customer.setSurname(request.getSurname());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }
        customer.setUpdatedAt(Instant.now());

        customerRepository.save(customer);

        return new MessageResponse(messageSource.getMessage("customerService.updateCustomer.statusUpdatedSuccessfully", null, locale));
    }

    public MessageResponse deleteCustomer(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        CustomerEntity customer = findActiveCustomer(id);
        customer.setIsVisible(false);
        customer.setStatus(CustomerStatus.CLOSED);
        customerRepository.save(customer);
        return new MessageResponse(messageSource.getMessage("customerService.deleteCustomer.customerDeleted", null, locale));
    }

    public CustomerEntity findActiveCustomer(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("customerService.findActiveCustomer.customerNotFound", null, locale)));
    }


}