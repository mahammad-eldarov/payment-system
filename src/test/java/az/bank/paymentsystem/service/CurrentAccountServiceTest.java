package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.mapper.CurrentAccountMapper;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountValidator;
import az.bank.paymentsystem.util.shared.CurrentAccountBalanceTransfer;
import az.bank.paymentsystem.util.shared.MessageUtil;
import az.bank.paymentsystem.util.shared.StatusAuditLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentAccountServiceTest {

    @Mock private CurrentAccountRepository currentAccountRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CurrentAccountMapper currentAccountMapper;
    @Mock private CurrentAccountValidator currentAccountValidator;
    @Mock private CurrentAccountBalanceTransfer currentAccountBalanceTransfer;
    @Mock private StatusAuditLogger statusAuditLogger;
    @Mock private MessageSource messageSource;
    @Mock private MessageUtil messageUtil;

    @Captor private ArgumentCaptor<CurrentAccountEntity> accountCaptor;
    @Captor private ArgumentCaptor<List<CurrentAccountEntity>> accountListCaptor;

    @InjectMocks
    private CurrentAccountService currentAccountService;

    private CustomerEntity customer;
    private CurrentAccountEntity account;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setLanguage(Language.EN);
        customer.setIsVisible(true);

        account = new CurrentAccountEntity();
        account.setId(1);
        account.setStatus(CurrentAccountStatus.ACTIVE);
        account.setIsVisible(true);
        account.setCustomer(customer);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
        lenient().when(messageUtil.resolveLocale(any(CustomerEntity.class)))
                .thenReturn(Language.EN.toLocale());
    }

    @Test
    void shouldReturnAccountResponseListWhenCustomerHasAccounts() {
        CurrentAccountResponse expected = new CurrentAccountResponse();
        expected.setId(1);

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));
        when(currentAccountRepository.findByCustomerIdAndIsVisibleTrue(1)).thenReturn(List.of(account));
        when(currentAccountMapper.toResponse(account)).thenReturn(expected);

        List<CurrentAccountResponse> actual = currentAccountService.getAccountsByCustomerId(1);

        assertEquals(1, actual.size());
        assertEquals(expected.getId(), actual.get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenCustomerHasNoAccounts() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));
        when(currentAccountRepository.findByCustomerIdAndIsVisibleTrue(1))
                .thenReturn(Collections.emptyList());

        assertThrows(EmptyListException.class,
                () -> currentAccountService.getAccountsByCustomerId(1));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForGetAccounts() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> currentAccountService.getAccountsByCustomerId(99));
    }

    @Test
    void shouldReturnAccountResponseWhenAccountFoundByAccountNumber() {
        CurrentAccountResponse expected = new CurrentAccountResponse();
        expected.setId(1);

        when(currentAccountRepository.findByAccountNumberAndIsVisibleTrue("AZ12BANK0000000001"))
                .thenReturn(Optional.of(account));
        when(currentAccountMapper.toResponse(account)).thenReturn(expected);

        CurrentAccountResponse actual =
                currentAccountService.getAccountByAccountNumber("AZ12BANK0000000001");

        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountNotFoundByAccountNumber() {
        when(currentAccountRepository.findByAccountNumberAndIsVisibleTrue("AZ00BANK0000000000"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> currentAccountService.getAccountByAccountNumber("AZ00BANK0000000000"));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetByStatus() {
        assertThrows(PageRequestException.class,
                () -> currentAccountService.getCurrentAccountByStatus(CurrentAccountStatus.ACTIVE, 0));
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenNoAccountsMatchGivenStatus() {
        when(currentAccountRepository.findByStatus(eq(CurrentAccountStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(AccountNotFoundException.class,
                () -> currentAccountService.getCurrentAccountByStatus(CurrentAccountStatus.ACTIVE, 1));
    }

    @Test
    void shouldReturnMappedPageWhenAccountsFoundByStatus() {
        CurrentAccountResponse expected = new CurrentAccountResponse();
        expected.setId(1);

        Page<CurrentAccountEntity> entityPage = new PageImpl<>(List.of(account));
        when(currentAccountRepository.findByStatus(eq(CurrentAccountStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(entityPage);
        when(currentAccountMapper.toResponse(account)).thenReturn(expected);

        Page<CurrentAccountResponse> actual =
                currentAccountService.getCurrentAccountByStatus(CurrentAccountStatus.ACTIVE, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldUpdateAccountStatusAndSaveWhenAccountIsActive() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual =
                currentAccountService.updateCurrentAccountStatus(1, CurrentAccountStatus.SUSPICIOUS);

        verify(currentAccountRepository).save(accountCaptor.capture());
        CurrentAccountEntity saved = accountCaptor.getValue();

        assertEquals(CurrentAccountStatus.SUSPICIOUS, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldCallStatusAuditLoggerWhenAccountStatusIsUpdated() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));

        currentAccountService.updateCurrentAccountStatus(1, CurrentAccountStatus.SUSPICIOUS);

        verify(statusAuditLogger).logAccount(eq(account), eq(CurrentAccountStatus.SUSPICIOUS.name()), anyString());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExistForStatusUpdate() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> currentAccountService.updateCurrentAccountStatus(99, CurrentAccountStatus.SUSPICIOUS));
    }

    @Test
    void shouldSetExpiredStatusAndSaveAllExpiredAccounts() {
        CurrentAccountEntity expiredAccount = new CurrentAccountEntity();
        expiredAccount.setId(2);
        expiredAccount.setStatus(CurrentAccountStatus.ACTIVE);
        expiredAccount.setCustomer(customer);

        when(currentAccountRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(CurrentAccountStatus.EXPIRED)))
                .thenReturn(List.of(expiredAccount));

        currentAccountService.updateExpiredCurrentAccounts();

        verify(currentAccountRepository).saveAll(accountListCaptor.capture());
        CurrentAccountEntity saved = accountListCaptor.getValue().get(0);

        assertEquals(CurrentAccountStatus.EXPIRED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCallBalanceTransferForEachExpiredAccount() {
        CurrentAccountEntity expiredAccount = new CurrentAccountEntity();
        expiredAccount.setId(2);
        expiredAccount.setStatus(CurrentAccountStatus.ACTIVE);
        expiredAccount.setCustomer(customer);

        when(currentAccountRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(CurrentAccountStatus.EXPIRED)))
                .thenReturn(List.of(expiredAccount));

        currentAccountService.updateExpiredCurrentAccounts();

        verify(currentAccountBalanceTransfer).transfer(eq(expiredAccount), any(Locale.class));
    }

    @Test
    void shouldNotCallBalanceTransferWhenNoExpiredAccountsFound() {
        when(currentAccountRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(CurrentAccountStatus.EXPIRED)))
                .thenReturn(Collections.emptyList());

        currentAccountService.updateExpiredCurrentAccounts();

        verify(currentAccountRepository).saveAll(Collections.emptyList());
        verify(currentAccountBalanceTransfer, never()).transfer(any(), any());
    }

    @Test
    void shouldSetAccountStatusClosedAndIsVisibleFalseWhenAccountIsDeleted() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));

        currentAccountService.deleteCurrentAccount(1);

        verify(currentAccountRepository).save(accountCaptor.capture());
        CurrentAccountEntity saved = accountCaptor.getValue();

        assertEquals(CurrentAccountStatus.CLOSED, saved.getStatus());
        assertFalse(saved.getIsVisible());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCallValidatorAndAuditLoggerWhenAccountIsDeleted() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));

        currentAccountService.deleteCurrentAccount(1);

        verify(currentAccountValidator).validateDeletion(account);
        verify(statusAuditLogger).logAccount(eq(account), eq(CurrentAccountStatus.CLOSED.name()), anyString());
    }

    @Test
    void shouldReturnMessageResponseWhenAccountIsDeleted() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual = currentAccountService.deleteCurrentAccount(1);

        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExistForDelete() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> currentAccountService.deleteCurrentAccount(99));
    }

    @Test
    void shouldReturnAccountEntityWhenActiveAccountIsFound() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));

        CurrentAccountEntity actual = currentAccountService.findActiveAccount(1);

        assertEquals(account.getId(), actual.getId());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenActiveAccountDoesNotExist() {
        when(currentAccountRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> currentAccountService.findActiveAccount(99));
    }

    @Test
    void shouldReturnAccountEntityWhenActiveAccountFoundByAccountNumber() {
        when(currentAccountRepository.findByAccountNumberAndIsVisibleTrue("AZ12BANK0000000001"))
                .thenReturn(Optional.of(account));

        CurrentAccountEntity actual =
                currentAccountService.findActiveAccountByNumber("AZ12BANK0000000001");

        assertEquals(account.getId(), actual.getId());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenActiveAccountNotFoundByAccountNumber() {
        when(currentAccountRepository.findByAccountNumberAndIsVisibleTrue("AZ00BANK0000000000"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> currentAccountService.findActiveAccountByNumber("AZ00BANK0000000000"));
    }

    @Test
    void shouldReturnCustomerEntityWhenActiveCustomerIsFound() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));

        CustomerEntity actual = currentAccountService.findActiveCustomer(1);

        assertEquals(customer.getId(), actual.getId());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenActiveCustomerDoesNotExist() {
        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> currentAccountService.findActiveCustomer(99));
    }
}