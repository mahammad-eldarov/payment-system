package az.bank.paymentsystem.service;

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

//    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerResponseBuilder customerResponseBuilder;
    private final CustomerCreator customerCreator;
    private final EntityFinderService entityFinderService;

    //Create
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        CustomerEntity customer = customerCreator.createCustomer(request);
        entityFinderService.saveCustomer(customer);
//        customerRepository.save(customer);

        CustomerResponse response = customerMapper.toResponse(customer);
        response.setPin(request.getPin());
        return response;
    }

    // GET

    public CustomerResponse getCustomerById(Integer id) {

//        Optional<CustomerEntity> activeCustomer = customerRepository.findByIdAndIsVisibleTrue(id);
        Optional<CustomerEntity> activeCustomer = entityFinderService.findCustomerIdVisibleTrue(id);
        if (activeCustomer.isPresent()) {
            return customerMapper.toResponse(activeCustomer.get());
        }
//        if (customerRepository.findByIdAndIsVisibleFalse(id).isPresent()) {
//            throw new CustomerDeletedException("This customer has been deleted.");
//        }
        if (entityFinderService.findCustomerIdVisibleFalse(id).isPresent()) {
            throw new CustomerDeletedException("This customer has been deleted.");
        }
        throw new CustomerNotFoundException("Customer not found");
    }

    public List<CustomerResponse> getCustomersByStatus(CustomerStatus status) {
//        List<CustomerEntity> activeCustomers = customerRepository.findByStatusAndIsVisibleTrue(status);

        List<CustomerEntity> activeCustomers = entityFinderService.findCustomerStatusVisibleTrue(status);
        if (!activeCustomers.isEmpty()) {
            return activeCustomers.stream().map(customerMapper::toResponse).collect(Collectors.toList());
        }
//        if (!customerRepository.findByStatusAndIsVisibleFalse(status).isEmpty()) {
//            throw new CustomerDeletedException("Customers with this status have been deleted.");
//        }
        if (!entityFinderService.findCustomerStatusVisibleFalse(status).isEmpty()) {
            throw new CustomerDeletedException("Customers with this status have been deleted.");
        }
        throw new CustomerNotFoundException("No customers found with this status");
    }

    public CustomerResponse getDeletedCustomerById(Integer id) {
//        CustomerEntity customer = customerRepository.findByIdAndIsVisibleFalse(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        CustomerEntity customer = entityFinderService.findCustomerIdVisibleFalse(id).orElseThrow(()-> new CustomerNotFoundException("Customer not found"));
        CustomerResponse response = customerMapper.toResponse(customer);
        customerResponseBuilder.setCardsAndAccounts(response, id);
        return response;
    }

    public List<CustomerResponse> getAllCustomers() {
//        List<CustomerEntity> activeCustomer = customerRepository.findAllByIsVisibleTrue();
        List<CustomerEntity> activeCustomer = entityFinderService.findAllCustomerVisibleTrue();

        if (activeCustomer.isEmpty()) {
            throw new EmptyListException("List is Empty.");
        }

        return activeCustomer.stream().map(customerMapper::toResponse).collect(Collectors.toList());
    }

    public CustomerResponse getCustomersCardsAndAccounts(Integer id) {
        CustomerResponse response = customerMapper.toResponse(entityFinderService.findActiveCustomer(id));
        customerResponseBuilder.setCardsAndAccounts(response, id);
        return response;
    }

    // Müştərinin kartlarını tranzaksiyalarla birlikdə gətirir
    public CustomerResponse getCustomerWithCardTransactions(Integer customerId) {
        CustomerResponse response = customerMapper.toResponse(entityFinderService.findActiveCustomer(customerId));
        customerResponseBuilder.setCardTransactions(response, customerId);
        return response;
    }

    // Müştərinin cari hesablarını tranzaksiyalarla birlikdə gətirir
    public CustomerResponse getCustomerWithAccountTransactions(Integer customerId) {
        CustomerResponse response = customerMapper.toResponse(entityFinderService.findActiveCustomer(customerId));
        customerResponseBuilder.setAccountTransactions(response, customerId);
        return response;
    }

    // UPDATE

    public MessageResponse updateCustomerStatus(Integer id, CustomerStatus status) {
        CustomerEntity customer = entityFinderService.findActiveCustomer(id);
        customer.setStatus(status);
        customer.setUpdatedAt(Instant.now());
        entityFinderService.saveCustomer(customer);
//        customerRepository.save(customer);
        return new MessageResponse("Status updated successfully");
    }

    public MessageResponse updateCustomer(Integer id, UpdateCustomerRequest request) {

        CustomerEntity customer = entityFinderService.findActiveCustomer(id);

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

        entityFinderService.saveCustomer(customer);
//        customerRepository.save(customer);

        return new MessageResponse("Customer information updated successfully.");
    }

    // DELETE

    public MessageResponse deleteCustomer(Integer id) {
        CustomerEntity customer = entityFinderService.findActiveCustomer(id);
        customer.setIsVisible(false);
        customer.setStatus(CustomerStatus.CLOSED);
        entityFinderService.saveCustomer(customer);
//        customerRepository.save(customer);
        return new MessageResponse("Customer information has been deleted successfully.");
    }

    // RESPONSE
//    public CustomerResponse toResponse(CustomerEntity customer) {
//        return customerMapper.toResponse(customer);
//    }

    // Auxiliary method
//    public CustomerEntity findActiveCustomer(Integer id) {
//        return customerRepository.findByIdAndIsVisibleTrue(id)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//    }


}