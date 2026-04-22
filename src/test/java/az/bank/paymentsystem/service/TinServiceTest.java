package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.TinResponse;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.exception.TinNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.mapper.TinMapper;
import az.bank.paymentsystem.repository.TinRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.shared.TinBalanceTransfer;
import az.bank.paymentsystem.util.shared.MessageUtil;
import az.bank.paymentsystem.util.shared.StatusAuditLogger;
import az.bank.paymentsystem.util.tin.TinValidator;
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
class TinServiceTest {

    @Mock private TinRepository tinRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private TinMapper tinMapper;
    @Mock private TinValidator tinValidator;
    @Mock private TinBalanceTransfer tinBalanceTransfer;
    @Mock private StatusAuditLogger statusAuditLogger;
    @Mock private MessageSource messageSource;
    @Mock private MessageUtil messageUtil;

    @Captor private ArgumentCaptor<TinEntity> tinCaptor;
    @Captor private ArgumentCaptor<List<TinEntity>> tinListCaptor;

    @InjectMocks
    private TinService tinService;

    private CustomerEntity customer;
    private TinEntity tin;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setLanguage(Language.EN);
        customer.setIsVisible(true);

        tin = new TinEntity();
        tin.setId(1);
        tin.setStatus(TinStatus.ACTIVE);
        tin.setIsVisible(true);
        tin.setCustomer(customer);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
        lenient().when(messageUtil.resolveLocale(any(CustomerEntity.class)))
                .thenReturn(Language.EN.toLocale());
    }

    @Test
    void shouldReturnTinResponseListWhenCustomerHasTin() {
        TinResponse expected = new TinResponse();
        expected.setId(1);

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));
        when(tinRepository.findByCustomerIdAndIsVisibleTrue(1)).thenReturn(List.of(tin));
        when(tinMapper.toResponse(tin)).thenReturn(expected);

        List<TinResponse> actual = tinService.getTinByCustomerId(1);

        assertEquals(1, actual.size());
        assertEquals(expected.getId(), actual.get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenCustomerHasNoTin() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));
        when(tinRepository.findByCustomerIdAndIsVisibleTrue(1))
                .thenReturn(Collections.emptyList());

        assertThrows(expected, () -> tinService.getTinByCustomerId(1));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForGetTin() {
        Class<CustomerNotFoundException> expected = CustomerNotFoundException.class;

        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> tinService.getTinByCustomerId(99));
    }

    @Test
    void shouldReturnTinResponseWhenTinFoundByTinNumber() {
        TinResponse expected = new TinResponse();
        expected.setId(1);

        when(tinRepository.findByTinNumberAndIsVisibleTrue("AZ12BANK0000000001"))
                .thenReturn(Optional.of(tin));
        when(tinMapper.toResponse(tin)).thenReturn(expected);

        TinResponse actual =
                tinService.getTinByTinNumber("AZ12BANK0000000001");

        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenTinNotFoundByTinNumber() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findByTinNumberAndIsVisibleTrue("AZ00BANK0000000000"))
                .thenReturn(Optional.empty());

        assertThrows(expected,
                () -> tinService.getTinByTinNumber("AZ00BANK0000000000"));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetByStatus() {
        Class<PageRequestException> expected = PageRequestException.class;

        assertThrows(expected,
                () -> tinService.getTinByStatus(TinStatus.ACTIVE, 0));
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenNoTinMatchGivenStatus() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findByStatus(eq(TinStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(expected,
                () -> tinService.getTinByStatus(TinStatus.ACTIVE, 1));
    }

    @Test
    void shouldReturnMappedPageWhenTinFoundByStatus() {
        TinResponse expected = new TinResponse();
        expected.setId(1);

        Page<TinEntity> entityPage = new PageImpl<>(List.of(tin));
        when(tinRepository.findByStatus(eq(TinStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(entityPage);
        when(tinMapper.toResponse(tin)).thenReturn(expected);

        Page<TinResponse> actual =
                tinService.getTinByStatus(TinStatus.ACTIVE, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldUpdateTinStatusAndSaveWhenTinIsActive() {
        when(tinRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(tin));

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual =
                tinService.updateTinStatus(1, TinStatus.SUSPICIOUS);

        verify(tinRepository).save(tinCaptor.capture());
        TinEntity saved = tinCaptor.getValue();

        assertEquals(TinStatus.SUSPICIOUS, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldCallStatusAuditLoggerWhenTinStatusIsUpdated() {
        when(tinRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(tin));

        tinService.updateTinStatus(1, TinStatus.SUSPICIOUS);

        verify(statusAuditLogger).logTin(eq(tin), eq(TinStatus.SUSPICIOUS.name()), anyString());
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenTinDoesNotExistForStatusUpdate() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected,
                () -> tinService.updateTinStatus(99, TinStatus.SUSPICIOUS));
    }

    @Test
    void shouldSetExpiredStatusAndSaveAllExpiredTin() {
        TinEntity expiredTin = new TinEntity();
        expiredTin.setId(2);
        expiredTin.setStatus(TinStatus.ACTIVE);
        expiredTin.setCustomer(customer);

        when(tinRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(TinStatus.EXPIRED)))
                .thenReturn(List.of(expiredTin));

        tinService.updateExpiredTin();

        verify(tinRepository).saveAll(tinListCaptor.capture());
        TinEntity saved = tinListCaptor.getValue().get(0);

        assertEquals(TinStatus.EXPIRED, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCallBalanceTransferForEachExpiredTin() {
        TinEntity expiredTin = new TinEntity();
        expiredTin.setId(2);
        expiredTin.setStatus(TinStatus.ACTIVE);
        expiredTin.setCustomer(customer);

        when(tinRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(TinStatus.EXPIRED)))
                .thenReturn(List.of(expiredTin));

        tinService.updateExpiredTin();

        verify(tinBalanceTransfer).transfer(eq(expiredTin), any(Locale.class));
    }

    @Test
    void shouldNotCallBalanceTransferWhenNoExpiredTinFound() {
        when(tinRepository.findAllByExpiryDateLessThanEqualAndStatusNot(
                any(LocalDate.class), eq(TinStatus.EXPIRED)))
                .thenReturn(Collections.emptyList());

        tinService.updateExpiredTin();

        verify(tinRepository).saveAll(Collections.emptyList());
        verify(tinBalanceTransfer, never()).transfer(any(), any());
    }

    @Test
    void shouldSetTinStatusClosedAndIsVisibleFalseWhenTinIsDeleted() {
        when(tinRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(tin));

        tinService.deleteTin(1);

        verify(tinRepository).save(tinCaptor.capture());
        TinEntity saved = tinCaptor.getValue();

        assertEquals(TinStatus.CLOSED, saved.getStatus());
        assertFalse(saved.getIsVisible());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCallValidatorAndAuditLoggerWhenTinIsDeleted() {
        when(tinRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(tin));

        tinService.deleteTin(1);

        verify(tinValidator).validateDeletion(tin);
        verify(statusAuditLogger).logTin(eq(tin), eq(TinStatus.CLOSED.name()), anyString());
    }

    @Test
    void shouldReturnMessageResponseWhenTinIsDeleted() {
        when(tinRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(tin));

        MessageResponse expected = new MessageResponse("mocked-message");

        MessageResponse actual = tinService.deleteTin(1);

        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenTinDoesNotExistForDelete() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> tinService.deleteTin(99));
    }

    @Test
    void shouldReturnTinEntityWhenActiveTinIsFound() {
        when(tinRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(tin));

        TinEntity actual = tinService.findActiveTin(1);

        assertEquals(tin.getId(), actual.getId());
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenActiveTinDoesNotExist() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> tinService.findActiveTin(99));
    }

    @Test
    void shouldReturnTinEntityWhenActiveTinFoundByTinNumber() {
        when(tinRepository.findByTinNumberAndIsVisibleTrue("AZ12BANK0000000001"))
                .thenReturn(Optional.of(tin));

        TinEntity actual =
                tinService.findActiveTinByNumber("AZ12BANK0000000001");

        assertEquals(tin.getId(), actual.getId());
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenActiveTinNotFoundByTinNumber() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findByTinNumberAndIsVisibleTrue("AZ00BANK0000000000"))
                .thenReturn(Optional.empty());

        assertThrows(expected,
                () -> tinService.findActiveTinByNumber("AZ00BANK0000000000"));
    }

    @Test
    void shouldReturnCustomerEntityWhenActiveCustomerIsFound() {
        when(customerRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(customer));

        CustomerEntity actual = tinService.findActiveCustomer(1);

        assertEquals(customer.getId(), actual.getId());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenActiveCustomerDoesNotExist() {
        Class<CustomerNotFoundException> expected = CustomerNotFoundException.class;

        when(customerRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> tinService.findActiveCustomer(99));
    }
}