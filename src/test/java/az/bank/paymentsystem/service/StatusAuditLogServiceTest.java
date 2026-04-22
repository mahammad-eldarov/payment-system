package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.StatusAuditLogResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.StatusAuditLogEntity;
import az.bank.paymentsystem.exception.TinNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.mapper.StatusAuditLogMapper;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.TinRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.StatusAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusAuditLogServiceTest {

    @Mock private StatusAuditLogRepository statusAuditLogRepository;
    @Mock private StatusAuditLogMapper statusAuditLogMapper;
    @Mock private CardRepository cardRepository;
    @Mock private TinRepository tinRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private MessageSource messageSource;

    @InjectMocks
    private StatusAuditLogService statusAuditLogService;

    private CardEntity card;
    private TinEntity tin;
    private CustomerEntity customer;
    private StatusAuditLogEntity log;

    @BeforeEach
    void setUp() {
        card = new CardEntity();
        card.setId(1);

        tin = new TinEntity();
        tin.setId(1);

        customer = new CustomerEntity();
        customer.setId(1);

        log = new StatusAuditLogEntity();
        log.setId(1);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
    }

    @Test
    void shouldReturnMappedPageWhenCardHasStatusHistory() {
        StatusAuditLogResponse expected = new StatusAuditLogResponse();
        expected.setId(1);

        Page<StatusAuditLogEntity> entityPage = new PageImpl<>(List.of(log));

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(statusAuditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                eq("CARD"), eq(1), any(Pageable.class))).thenReturn(entityPage);
        when(statusAuditLogMapper.toResponse(log)).thenReturn(expected);

        Page<StatusAuditLogResponse> actual = statusAuditLogService.getCardHistory(1, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenCardHasNoStatusHistory() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(statusAuditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                eq("CARD"), eq(1), any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(expected, () -> statusAuditLogService.getCardHistory(1, 1));
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExistForHistory() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> statusAuditLogService.getCardHistory(99, 1));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForCardHistory() {
        Class<PageRequestException> expected = PageRequestException.class;

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));

        assertThrows(expected, () -> statusAuditLogService.getCardHistory(1, 0));
    }

    @Test
    void shouldReturnMappedPageWhenTinHasStatusHistory() {
        StatusAuditLogResponse expected = new StatusAuditLogResponse();
        expected.setId(1);

        Page<StatusAuditLogEntity> entityPage = new PageImpl<>(List.of(log));

        when(tinRepository.findById(1)).thenReturn(Optional.of(tin));
        when(statusAuditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                eq("TIN"), eq(1), any(Pageable.class))).thenReturn(entityPage);
        when(statusAuditLogMapper.toResponse(log)).thenReturn(expected);

        Page<StatusAuditLogResponse> actual = statusAuditLogService.getTinHistory(1, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenTinHasNoStatusHistory() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(tinRepository.findById(1)).thenReturn(Optional.of(tin));
        when(statusAuditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                eq("TIN"), eq(1), any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(expected, () -> statusAuditLogService.getTinHistory(1, 1));
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenTinDoesNotExistForHistory() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> statusAuditLogService.getTinHistory(99, 1));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForTinHistory() {
        Class<PageRequestException> expected = PageRequestException.class;

        when(tinRepository.findById(1)).thenReturn(Optional.of(tin));

        assertThrows(expected, () -> statusAuditLogService.getTinHistory(1, 0));
    }

    @Test
    void shouldReturnMappedPageWhenCustomerHasStatusHistory() {
        StatusAuditLogResponse expected = new StatusAuditLogResponse();
        expected.setId(1);

        Page<StatusAuditLogEntity> entityPage = new PageImpl<>(List.of(log));

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(statusAuditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                eq("CUSTOMER"), eq(1), any(Pageable.class))).thenReturn(entityPage);
        when(statusAuditLogMapper.toResponse(log)).thenReturn(expected);

        Page<StatusAuditLogResponse> actual = statusAuditLogService.getCustomerHistory(1, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenCustomerHasNoStatusHistory() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(statusAuditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                eq("CUSTOMER"), eq(1), any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(expected, () -> statusAuditLogService.getCustomerHistory(1, 1));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExistForHistory() {
        Class<CustomerNotFoundException> expected = CustomerNotFoundException.class;

        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> statusAuditLogService.getCustomerHistory(99, 1));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForCustomerHistory() {
        Class<PageRequestException> expected = PageRequestException.class;

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

        assertThrows(expected, () -> statusAuditLogService.getCustomerHistory(1, 0));
    }

    @Test
    void shouldNotThrowWhenCardExists() {
        when(cardRepository.findById(1)).thenReturn(Optional.of(card));

        assertDoesNotThrow(() -> statusAuditLogService.findCard(1));
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExist() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> statusAuditLogService.findCard(99));
    }

    @Test
    void shouldNotThrowWhenCustomerExists() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

        assertDoesNotThrow(() -> statusAuditLogService.findCustomer(1));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        Class<CustomerNotFoundException> expected = CustomerNotFoundException.class;

        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> statusAuditLogService.findCustomer(99));
    }

    @Test
    void shouldNotThrowWhenTinExists() {
        when(tinRepository.findById(1)).thenReturn(Optional.of(tin));

        assertDoesNotThrow(() -> statusAuditLogService.findTin(1));
    }

    @Test
    void shouldThrowTinNotFoundExceptionWhenTinDoesNotExist() {
        Class<TinNotFoundException> expected = TinNotFoundException.class;

        when(tinRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> statusAuditLogService.findTin(99));
    }
}