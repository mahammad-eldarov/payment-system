package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.CreateCustomerRequest;
import az.bank.paymentsystem.dto.request.UpdateCustomerRequest;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.dto.response.CustomerShortResponse;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.exception.CustomerDeletedException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.mapper.CustomerMapper;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.customer.CustomerCreator;
import az.bank.paymentsystem.util.customer.CustomerResponseBuilder;
import az.bank.paymentsystem.util.shared.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerMapper customerMapper;
    @Mock private CustomerResponseBuilder customerResponseBuilder;
    @Mock private CustomerCreator customerCreator;
    @Mock private MessageSource messageSource;
    @Mock private MessageUtil messageUtil;

    @InjectMocks
    private CustomerService customerService;

    private CustomerEntity activeCustomer;
    private CustomerEntity deletedCustomer;

    @BeforeEach
    void setUp() {
        activeCustomer = new CustomerEntity();
        activeCustomer.setId(1);
        activeCustomer.setName("John");
        activeCustomer.setSurname("Doe");
        activeCustomer.setEmail("john.doe@example.com");
        activeCustomer.setPhoneNumber("+994501234567");
        activeCustomer.setStatus(CustomerStatus.ACTIVE);
        activeCustomer.setLanguage(Language.EN);
        activeCustomer.setIsVisible(true);

        deletedCustomer = new CustomerEntity();
        deletedCustomer.setId(2);
        deletedCustomer.setIsVisible(false);
        deletedCustomer.setStatus(CustomerStatus.CLOSED);
        deletedCustomer.setLanguage(Language.EN);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
    }

    @Test
    void shouldSaveCustomerAndReturnResponseWithPin() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setPin("12345678");

        CustomerShortResponse expected = new CustomerShortResponse();
        expected.setId(1);

        when(customerCreator.createCustomer(request)).thenReturn(activeCustomer);
        when(customerMapper.toShortResponse(activeCustomer)).thenReturn(expected);

        CustomerShortResponse actual = customerService.createCustomer(request);

        verify(customerRepository).save(activeCustomer);
        assertEquals("12345678", actual.getPin());
    }

    @Test
    void shouldReturnShortResponseWhenCustomerIsActive() {
        CustomerShortResponse expected = new CustomerShortResponse();
        expected.setId(1);

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));
        when(customerMapper.toShortResponse(activeCustomer)).thenReturn(expected);

        CustomerShortResponse actual = customerService.getCustomerById(1);

        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowCustomerDeletedExceptionWhenCustomerIsSoftDeleted() {
        when(customerRepository.findByIdAndIsVisibleTrue(2)).thenReturn(Optional.empty());
        when(customerRepository.findByIdAndIsVisibleFalse(2)).thenReturn(Optional.of(deletedCustomer));

        assertThrows(CustomerDeletedException.class, () -> customerService.getCustomerById(2));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());
        when(customerRepository.findByIdAndIsVisibleFalse(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(99));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetByStatus() {
        assertThrows(PageRequestException.class,
                () -> customerService.getCustomersByStatus(CustomerStatus.ACTIVE, 0));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenNoCustomersMatchGivenStatus() {
        when(customerRepository.findByStatus(eq(CustomerStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomersByStatus(CustomerStatus.ACTIVE, 1));
    }

    @Test
    void shouldReturnMappedPageWhenCustomersFoundByStatus() {
        CustomerShortResponse expected = new CustomerShortResponse();
        expected.setId(1);

        Page<CustomerEntity> entityPage = new PageImpl<>(List.of(activeCustomer));
        when(customerRepository.findByStatus(eq(CustomerStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(entityPage);
        when(customerMapper.toShortResponse(activeCustomer)).thenReturn(expected);

        Page<CustomerShortResponse> actual = customerService.getCustomersByStatus(CustomerStatus.ACTIVE, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldReturnFullResponseWithCardsAndAccountsForDeletedCustomer() {
        CustomerResponse expected = new CustomerResponse();
        expected.setId(2);

        when(customerRepository.findByIdAndIsVisibleFalse(2)).thenReturn(Optional.of(deletedCustomer));
        when(customerMapper.toResponse(deletedCustomer)).thenReturn(expected);

        CustomerResponse actual = customerService.getDeletedCustomerById(2);

        verify(customerResponseBuilder).setCardsAndAccounts(expected, 2, deletedCustomer);
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenDeletedCustomerDoesNotExist() {
        when(customerRepository.findByIdAndIsVisibleFalse(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getDeletedCustomerById(99));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetAll() {
        assertThrows(PageRequestException.class, () -> customerService.getAllCustomers(0));
    }

    @Test
    void shouldThrowEmptyListExceptionWhenNoActiveCustomersExist() {
        when(customerRepository.findAllByIsVisibleTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        assertThrows(EmptyListException.class, () -> customerService.getAllCustomers(1));
    }

    @Test
    void shouldReturnMappedPageOfAllActiveCustomers() {
        CustomerShortResponse expected = new CustomerShortResponse();
        expected.setId(1);

        Page<CustomerEntity> entityPage = new PageImpl<>(List.of(activeCustomer));
        when(customerRepository.findAllByIsVisibleTrue(any(Pageable.class))).thenReturn(entityPage);
        when(customerMapper.toShortResponse(activeCustomer)).thenReturn(expected);

        Page<CustomerShortResponse> actual = customerService.getAllCustomers(1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldReturnCustomerResponseWithCardsAndAccountsWhenCustomerIsActive() {
        CustomerResponse expected = new CustomerResponse();
        expected.setId(1);

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));
        when(customerMapper.toResponse(activeCustomer)).thenReturn(expected);

        CustomerResponse actual = customerService.getCustomersCardsAndAccounts(1);

        verify(customerResponseBuilder).setCardsAndAccounts(expected, 1, activeCustomer);
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForCardsAndAccounts() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomersCardsAndAccounts(99));
    }

    @Test
    void shouldReturnCardTransactionsWhenCustomerIsActive() {
        TransactionResponse transactionResponse = new TransactionResponse();
        Page<TransactionResponse> expected = new PageImpl<>(List.of(transactionResponse));

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));
        when(customerResponseBuilder.buildCardTransactions(1, "4000123456789012", 1))
                .thenReturn(expected);

        Page<TransactionResponse> actual = customerService.getCardTransactions(1, "4000123456789012", 1);

        assertEquals(expected.getTotalElements(), actual.getTotalElements());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForCardTransactions() {
        when(customerRepository.findByIdAndIsVisibleTrue(5)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCardTransactions(5, "4000000000000001", 1));
    }

    @Test
    void shouldReturnAccountTransactionsWhenCustomerIsActive() {
        TransactionResponse transactionResponse = new TransactionResponse();
        Page<TransactionResponse> expected = new PageImpl<>(List.of(transactionResponse));

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));
        when(customerResponseBuilder.buildAccountTransactions(1, "AZ12BANK0000000001", 1))
                .thenReturn(expected);

        Page<TransactionResponse> actual = customerService.getAccountTransactions(1, "AZ12BANK0000000001", 1);

        assertEquals(expected.getTotalElements(), actual.getTotalElements());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForAccountTransactions() {
        when(customerRepository.findByIdAndIsVisibleTrue(5)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getAccountTransactions(5, "AZ12BANK0000000002", 1));
    }

    @Test
    void shouldUpdateCustomerStatusAndSaveWhenCustomerIsActive() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual = customerService.updateCustomerStatus(1, CustomerStatus.BLOCKED);

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());
        CustomerEntity saved = captor.getValue();

        assertEquals(CustomerStatus.BLOCKED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForStatusUpdate() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomerStatus(99, CustomerStatus.BLOCKED));
    }

    @Test
    void shouldUpdateOnlyNonNullFieldsWhenSomeFieldsAreNull() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));
        when(messageUtil.resolveLocale(activeCustomer)).thenReturn(Language.EN.toLocale());

        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setName("James");

        customerService.updateCustomer(1, request);

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());
        CustomerEntity saved = captor.getValue();

        assertEquals("James", saved.getName());
        assertEquals("Doe", saved.getSurname());
        assertEquals("john.doe@example.com", saved.getEmail());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldUpdateAllFieldsWhenAllRequestFieldsAreProvided() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));
        when(messageUtil.resolveLocale(activeCustomer)).thenReturn(Language.EN.toLocale());

        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setName("New Name");
        request.setSurname("New Surname");
        request.setEmail("new@example.com");
        request.setPhoneNumber("+994559999999");
        request.setLanguage(Language.AZ);

        customerService.updateCustomer(1, request);

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());
        CustomerEntity saved = captor.getValue();

        assertEquals("New Name", saved.getName());
        assertEquals("New Surname", saved.getSurname());
        assertEquals("new@example.com", saved.getEmail());
        assertEquals("+994559999999", saved.getPhoneNumber());
        assertEquals(Language.AZ, saved.getLanguage());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForUpdate() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(99, new UpdateCustomerRequest()));
    }

    @Test
    void shouldSetIsVisibleFalseAndStatusClosedWhenCustomerIsDeleted() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));
        when(messageUtil.resolveLocale(activeCustomer)).thenReturn(Language.EN.toLocale());

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual = customerService.deleteCustomer(1);

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());
        CustomerEntity saved = captor.getValue();

        assertFalse(saved.getIsVisible());
        assertEquals(CustomerStatus.CLOSED, saved.getStatus());
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForDelete() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomer(99));
    }

    @Test
    void shouldReturnActiveCustomerEntityWhenFound() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(activeCustomer));

        CustomerEntity expected = activeCustomer;

        CustomerEntity actual = customerService.findActiveCustomer(1);

        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenActiveCustomerDoesNotExist() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.findActiveCustomer(99));
    }
}