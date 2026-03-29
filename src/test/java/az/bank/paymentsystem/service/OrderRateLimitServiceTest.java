package az.bank.paymentsystem.service;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.OrderRateLimitEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CardOrderCooldownException;
import az.bank.paymentsystem.repository.OrderRateLimitRepository;
import az.bank.paymentsystem.util.shared.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRateLimitServiceTest {

    @Mock private OrderRateLimitRepository orderRateLimitRepository;
    @Mock private MessageSource messageSource;
    @Mock private MessageUtil messageUtil;

    @Captor private ArgumentCaptor<OrderRateLimitEntity> limitCaptor;

    @InjectMocks
    private OrderRateLimitService orderRateLimitService;

    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setLanguage(Language.EN);
        customer.setIsVisible(true);

        lenient().when(messageUtil.resolveLocale(any(CustomerEntity.class)))
                .thenReturn(Language.EN.toLocale());
        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
    }

    @Test
    void shouldNotThrowWhenNoRateLimitRecordExists() {
        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> orderRateLimitService.checkCooldown(customer, OrderType.CARD));
    }

    @Test
    void shouldNotThrowWhenCooldownUntilIsNull() {
        OrderRateLimitEntity limit = new OrderRateLimitEntity(customer, OrderType.CARD);
        limit.setCooldownUntil(null);

        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.of(limit));

        assertDoesNotThrow(() -> orderRateLimitService.checkCooldown(customer, OrderType.CARD));
    }

    @Test
    void shouldThrowCardOrderCooldownExceptionWhenCooldownIsStillActive() {
        Class<CardOrderCooldownException> expected = CardOrderCooldownException.class;

        OrderRateLimitEntity limit = new OrderRateLimitEntity(customer, OrderType.CARD);
        limit.setCooldownUntil(Instant.now().plusSeconds(3600));

        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.of(limit));

        assertThrows(expected, () -> orderRateLimitService.checkCooldown(customer, OrderType.CARD));
    }

    @Test
    void shouldResetLimitWhenCooldownHasExpired() {
        OrderRateLimitEntity limit = new OrderRateLimitEntity(customer, OrderType.CARD);
        limit.setCooldownUntil(Instant.now().minusSeconds(1));

        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.of(limit));

        orderRateLimitService.checkCooldown(customer, OrderType.CARD);

        verify(orderRateLimitRepository).save(limitCaptor.capture());
        OrderRateLimitEntity saved = limitCaptor.getValue();

        assertEquals(0, saved.getRejectionCount());
        assertNull(saved.getCooldownUntil());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldIncrementRejectionCountWhenHandleRejectionCalledWithExistingLimit() {
        OrderRateLimitEntity limit = new OrderRateLimitEntity(customer, OrderType.CARD);
        limit.setRejectionCount(1);

        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.of(limit));

        orderRateLimitService.handleRejection(customer, OrderType.CARD);

        verify(orderRateLimitRepository).save(limitCaptor.capture());
        OrderRateLimitEntity saved = limitCaptor.getValue();

        assertEquals(2, saved.getRejectionCount());
        assertNull(saved.getCooldownUntil());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldCreateNewLimitAndSetRejectionCountToOneWhenNoExistingRecord() {
        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.empty());

        orderRateLimitService.handleRejection(customer, OrderType.CARD);

        verify(orderRateLimitRepository).save(limitCaptor.capture());
        OrderRateLimitEntity saved = limitCaptor.getValue();

        assertEquals(1, saved.getRejectionCount());
        assertNull(saved.getCooldownUntil());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldSetCooldownWhenRejectionCountReachesThree() {
        OrderRateLimitEntity limit = new OrderRateLimitEntity(customer, OrderType.CARD);
        limit.setRejectionCount(2);

        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.of(limit));

        orderRateLimitService.handleRejection(customer, OrderType.CARD);

        verify(orderRateLimitRepository).save(limitCaptor.capture());
        OrderRateLimitEntity saved = limitCaptor.getValue();

        assertEquals(3, saved.getRejectionCount());
        assertNotNull(saved.getCooldownUntil());
        assertTrue(saved.getCooldownUntil().isAfter(Instant.now()));
    }

    @Test
    void shouldNotSetCooldownWhenRejectionCountIsLessThanThree() {
        OrderRateLimitEntity limit = new OrderRateLimitEntity(customer, OrderType.CARD);
        limit.setRejectionCount(1);

        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.of(limit));

        orderRateLimitService.handleRejection(customer, OrderType.CARD);

        verify(orderRateLimitRepository).save(limitCaptor.capture());
        OrderRateLimitEntity saved = limitCaptor.getValue();

        assertNull(saved.getCooldownUntil());
    }

    @Test
    void shouldResetRejectionCountAndCooldownWhenResetLimitCalled() {
        OrderRateLimitEntity limit = new OrderRateLimitEntity(customer, OrderType.CARD);
        limit.setRejectionCount(3);
        limit.setCooldownUntil(Instant.now().plusSeconds(3600));

        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.of(limit));

        orderRateLimitService.resetLimit(customer, OrderType.CARD);

        verify(orderRateLimitRepository).save(limitCaptor.capture());
        OrderRateLimitEntity saved = limitCaptor.getValue();

        assertEquals(0, saved.getRejectionCount());
        assertNull(saved.getCooldownUntil());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldNotSaveWhenResetLimitCalledButNoRecordExists() {
        when(orderRateLimitRepository.findByCustomerIdAndOrderType(1, OrderType.CARD))
                .thenReturn(Optional.empty());

        orderRateLimitService.resetLimit(customer, OrderType.CARD);

        verify(orderRateLimitRepository, never()).save(any());
    }
}