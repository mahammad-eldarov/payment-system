package az.bank.paymentsystem.service;

import az.bank.paymentsystem.repository.CustomerRepository;
import java.time.Instant;
import java.util.List;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerResponseBuilder customerResponseBuilder;
    private final CustomerCreator customerCreator;
    //Create
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        CustomerEntity customer = customerCreator.createCustomer(request);
        customerRepository.save(customer);

        CustomerResponse response = customerMapper.toResponse(customer);
        response.setPin(request.getPin());
        return response;
    }

    // GET

    public CustomerResponse getCustomerById(Integer id) {

        Optional<CustomerEntity> activeCustomer = customerRepository.findByIdAndIsVisibleTrue(id);
        if (activeCustomer.isPresent()) {
            return customerMapper.toResponse(activeCustomer.get());
        }
        if (customerRepository.findByIdAndIsVisibleFalse(id).isPresent()) {
            throw new CustomerDeletedException("This customer has been deleted.");
        }
        throw new CustomerNotFoundException("Customer not found");
    }

    public List<CustomerResponse> getCustomersByStatus(CustomerStatus status) {
        List<CustomerEntity> activeCustomers = customerRepository.findByStatusAndIsVisibleTrue(status);

        if (!activeCustomers.isEmpty()) {
            return activeCustomers.stream().map(customerMapper::toResponse).collect(Collectors.toList());
        }
        if (!customerRepository.findByStatusAndIsVisibleFalse(status).isEmpty()) {
            throw new CustomerDeletedException("Customers with this status have been deleted.");
        }
        throw new CustomerNotFoundException("No customers found with this status");
    }

    public CustomerResponse getDeletedCustomerById(Integer id) {
        CustomerEntity customer = customerRepository.findByIdAndIsVisibleFalse(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        CustomerResponse response = customerMapper.toResponse(customer);
        customerResponseBuilder.setCardsAndAccounts(response, id);
        return response;
    }

    public List<CustomerResponse> getAllCustomers() {
        List<CustomerEntity> activeCustomer = customerRepository.findAllByIsVisibleTrue();

        if (activeCustomer.isEmpty()) {
            throw new EmptyListException("List is Empty.");
        }

        return activeCustomer.stream().map(customerMapper::toResponse).collect(Collectors.toList());
    }

    public CustomerResponse getCustomersCardsAndAccounts(Integer id) {
        CustomerResponse response = customerMapper.toResponse(findActiveCustomer(id));
        customerResponseBuilder.setCardsAndAccounts(response, id);
        return response;
    }

    // Müştərinin kartlarını tranzaksiyalarla birlikdə gətirir
    public CustomerResponse getCustomerWithCardTransactions(Integer customerId) {
        CustomerResponse response = customerMapper.toResponse(findActiveCustomer(customerId));
        customerResponseBuilder.setCardTransactions(response, customerId);
        return response;
    }

    // Müştərinin cari hesablarını tranzaksiyalarla birlikdə gətirir
    public CustomerResponse getCustomerWithAccountTransactions(Integer customerId) {
        CustomerResponse response = customerMapper.toResponse(findActiveCustomer(customerId));
        customerResponseBuilder.setAccountTransactions(response, customerId);
        return response;
    }

    // UPDATE

    public MessageResponse updateCustomerStatus(Integer id, CustomerStatus status) {
        CustomerEntity customer = findActiveCustomer(id);
        customer.setStatus(status);
        customer.setUpdatedAt(Instant.now());
        customerRepository.save(customer);
        return new MessageResponse("Status updated successfully");
    }

    public MessageResponse updateCustomer(Integer id, UpdateCustomerRequest request) {

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

        return new MessageResponse("Customer information updated successfully.");
    }

    // DELETE

    public MessageResponse deleteCustomer(Integer id) {
        CustomerEntity customer = findActiveCustomer(id);
        customer.setIsVisible(false);
        customer.setStatus(CustomerStatus.CLOSED);
        customerRepository.save(customer);
        return new MessageResponse("Customer information has been deleted successfully.");
    }


    // Auxiliary method
    public CustomerEntity findActiveCustomer(Integer id) {
        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }


}