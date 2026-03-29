package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.request.AccountToAccountRequest;
import az.bank.paymentsystem.dto.request.AccountToCardRequest;
import az.bank.paymentsystem.dto.request.CardToAccountRequest;
import az.bank.paymentsystem.dto.request.CardToCardRequest;
import az.bank.paymentsystem.dto.response.PaymentResponse;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.PaymentNotFoundException;
import az.bank.paymentsystem.mapper.PaymentMapper;
import az.bank.paymentsystem.repository.PaymentRepository;
import az.bank.paymentsystem.util.payment.PaymentCooldownChecker;
import az.bank.paymentsystem.util.payment.PaymentCreator;
import az.bank.paymentsystem.util.payment.PaymentProcessor;
import az.bank.paymentsystem.util.payment.PaymentSourceResolver;
import az.bank.paymentsystem.util.payment.PaymentValidator;
import az.bank.paymentsystem.util.shared.MessageUtil;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentValidator paymentValidator;
    @Mock private PaymentProcessor paymentProcessor;
    @Mock private PaymentSourceResolver paymentSourceResolver;
    @Mock private PaymentMapper paymentMapper;
    @Mock private PaymentCreator paymentCreator;
    @Mock private PaymentCooldownChecker paymentCooldownChecker;
    @Mock private MessageSource messageSource;
    @Mock private CustomerService customerService;
    @Mock private MessageUtil messageUtil;

    @InjectMocks
    private PaymentService paymentService;

    private CustomerEntity customer;
    private PaymentEntity payment;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setLanguage(Language.EN);
        customer.setIsVisible(true);

        payment = new PaymentEntity();
        payment.setId(1);
        payment.setStatus(PaymentStatus.PENDING);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
        lenient().when(messageUtil.resolveLocale(any(CustomerEntity.class)))
                .thenReturn(Language.EN.toLocale());
        lenient().when(paymentCooldownChecker.isInCooldown(anyInt(), any(), anyString(), anyString()))
                .thenReturn(false);
        lenient().when(paymentRepository.existsByIdempotencyKey(anyString()))
                .thenReturn(false);
    }

    @Test
    void shouldSaveAndReturnPaymentResponseWhenCardToCardIsSuccessful() {
        CardToCardRequest request = new CardToCardRequest();
        request.setFromPan("4000000000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentCreator.buildPayment(eq(1), eq(BigDecimal.TEN),
                eq(PaymentSourceType.CARD), eq(PaymentSourceType.CARD), anyString()))
                .thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.cardToCard(1, request);

        verify(paymentSourceResolver).fromCheckCard(eq(payment), eq(1), eq("4000000000000001"), anyList());
        verify(paymentSourceResolver).toCheckCard(eq(payment), eq("4000000000000002"), anyList());
        verify(paymentValidator).checkSelfTransfer(eq(payment), anyList());
        verify(paymentRepository).save(payment);
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldReturnExistingPaymentWhenCardToCardIdempotencyKeyAlreadyExists() {
        CardToCardRequest request = new CardToCardRequest();
        request.setFromPan("4000000000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentRepository.existsByIdempotencyKey(anyString())).thenReturn(true);
        when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.cardToCard(1, request);

        verify(paymentRepository, never()).save(any());
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenCardToCardCooldownIsActive() {
        CardToCardRequest request = new CardToCardRequest();
        request.setFromPan("4000000000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        when(paymentCooldownChecker.isInCooldown(anyInt(), any(), anyString(), anyString()))
                .thenReturn(true);

        assertThrows(MultiValidationException.class, () -> paymentService.cardToCard(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenCardToCardValidationFails() {
        CardToCardRequest request = new CardToCardRequest();
        request.setFromPan("4000000000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        when(paymentCreator.buildPayment(anyInt(), any(), any(), any(), anyString()))
                .thenReturn(payment);

        doAnswer(invocation -> {
            List<ExceptionResponse> errors = invocation.getArgument(1);
            errors.add(new ExceptionResponse(400, "validation error", LocalDateTime.now()));
            return null;
        }).when(paymentValidator).validateAmount(any(), anyList());

        assertThrows(MultiValidationException.class, () -> paymentService.cardToCard(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldSaveAndReturnPaymentResponseWhenCardToAccountIsSuccessful() {
        CardToAccountRequest request = new CardToAccountRequest();
        request.setFromPan("4000000000000001");
        request.setToAccountNumber("AZ12BANK0000000001");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentCreator.buildPayment(eq(1), eq(BigDecimal.TEN),
                eq(PaymentSourceType.CARD), eq(PaymentSourceType.CURRENT_ACCOUNT), anyString()))
                .thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.cardToAccount(1, request);

        verify(paymentSourceResolver).fromCheckCard(eq(payment), eq(1), eq("4000000000000001"), anyList());
        verify(paymentSourceResolver).toCheckAccount(eq(payment), eq("AZ12BANK0000000001"), anyList());
        verify(paymentValidator).checkSelfTransfer(eq(payment), anyList());
        verify(paymentRepository).save(payment);
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldReturnExistingPaymentWhenCardToAccountIdempotencyKeyAlreadyExists() {
        CardToAccountRequest request = new CardToAccountRequest();
        request.setFromPan("4000000000000001");
        request.setToAccountNumber("AZ12BANK0000000001");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentRepository.existsByIdempotencyKey(anyString())).thenReturn(true);
        when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.cardToAccount(1, request);

        verify(paymentRepository, never()).save(any());
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenCardToAccountCooldownIsActive() {
        CardToAccountRequest request = new CardToAccountRequest();
        request.setFromPan("4000000000000001");
        request.setToAccountNumber("AZ12BANK0000000001");
        request.setAmount(BigDecimal.TEN);

        when(paymentCooldownChecker.isInCooldown(anyInt(), any(), anyString(), anyString()))
                .thenReturn(true);

        assertThrows(MultiValidationException.class, () -> paymentService.cardToAccount(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenCardToAccountValidationFails() {
        CardToAccountRequest request = new CardToAccountRequest();
        request.setFromPan("4000000000000001");
        request.setToAccountNumber("AZ12BANK0000000001");
        request.setAmount(BigDecimal.TEN);

        when(paymentCreator.buildPayment(anyInt(), any(), any(), any(), anyString()))
                .thenReturn(payment);

        doAnswer(invocation -> {
            List<ExceptionResponse> errors = invocation.getArgument(1);
            errors.add(new ExceptionResponse(400, "validation error", LocalDateTime.now()));
            return null;
        }).when(paymentValidator).validateAmount(any(), anyList());

        assertThrows(MultiValidationException.class, () -> paymentService.cardToAccount(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldSaveAndReturnPaymentResponseWhenAccountToCardIsSuccessful() {
        AccountToCardRequest request = new AccountToCardRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentCreator.buildPayment(eq(1), eq(BigDecimal.TEN),
                eq(PaymentSourceType.CURRENT_ACCOUNT), eq(PaymentSourceType.CARD), anyString()))
                .thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.accountToCard(1, request);

        verify(paymentSourceResolver).fromCheckAccount(eq(payment), eq(1), eq("AZ12BANK0000000001"), anyList());
        verify(paymentSourceResolver).toCheckCard(eq(payment), eq("4000000000000002"), anyList());
        verify(paymentValidator).checkSelfTransfer(eq(payment), anyList());
        verify(paymentRepository).save(payment);
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldReturnExistingPaymentWhenAccountToCardIdempotencyKeyAlreadyExists() {
        AccountToCardRequest request = new AccountToCardRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentRepository.existsByIdempotencyKey(anyString())).thenReturn(true);
        when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.accountToCard(1, request);

        verify(paymentRepository, never()).save(any());
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenAccountToCardCooldownIsActive() {
        AccountToCardRequest request = new AccountToCardRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        when(paymentCooldownChecker.isInCooldown(anyInt(), any(), anyString(), anyString()))
                .thenReturn(true);

        assertThrows(MultiValidationException.class, () -> paymentService.accountToCard(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenAccountToCardValidationFails() {
        AccountToCardRequest request = new AccountToCardRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        when(paymentCreator.buildPayment(anyInt(), any(), any(), any(), anyString()))
                .thenReturn(payment);

        doAnswer(invocation -> {
            List<ExceptionResponse> errors = invocation.getArgument(1);
            errors.add(new ExceptionResponse(400, "validation error", LocalDateTime.now()));
            return null;
        }).when(paymentValidator).validateAmount(any(), anyList());

        assertThrows(MultiValidationException.class, () -> paymentService.accountToCard(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldSaveAndReturnPaymentResponseWhenAccountToAccountIsSuccessful() {
        AccountToAccountRequest request = new AccountToAccountRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToAccountNumber("AZ12BANK0000000002");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentCreator.buildPayment(eq(1), eq(BigDecimal.TEN),
                eq(PaymentSourceType.CURRENT_ACCOUNT), eq(PaymentSourceType.CURRENT_ACCOUNT), anyString()))
                .thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.accountToAccount(1, request);

        verify(paymentSourceResolver).fromCheckAccount(eq(payment), eq(1), eq("AZ12BANK0000000001"), anyList());
        verify(paymentSourceResolver).toCheckAccount(eq(payment), eq("AZ12BANK0000000002"), anyList());
        verify(paymentValidator).checkSelfTransfer(eq(payment), anyList());
        verify(paymentRepository).save(payment);
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldReturnExistingPaymentWhenAccountToAccountIdempotencyKeyAlreadyExists() {
        AccountToAccountRequest request = new AccountToAccountRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToAccountNumber("AZ12BANK0000000002");
        request.setAmount(BigDecimal.TEN);

        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(paymentRepository.existsByIdempotencyKey(anyString())).thenReturn(true);
        when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.accountToAccount(1, request);

        verify(paymentRepository, never()).save(any());
        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenAccountToAccountCooldownIsActive() {
        AccountToAccountRequest request = new AccountToAccountRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToAccountNumber("AZ12BANK0000000002");
        request.setAmount(BigDecimal.TEN);

        when(paymentCooldownChecker.isInCooldown(anyInt(), any(), anyString(), anyString()))
                .thenReturn(true);

        assertThrows(MultiValidationException.class, () -> paymentService.accountToAccount(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldThrowMultiValidationExceptionWhenAccountToAccountValidationFails() {
        AccountToAccountRequest request = new AccountToAccountRequest();
        request.setFromAccountNumber("AZ12BANK0000000001");
        request.setToAccountNumber("AZ12BANK0000000002");
        request.setAmount(BigDecimal.TEN);

        when(paymentCreator.buildPayment(anyInt(), any(), any(), any(), anyString()))
                .thenReturn(payment);

        doAnswer(invocation -> {
            List<ExceptionResponse> errors = invocation.getArgument(1);
            errors.add(new ExceptionResponse(400, "validation error", LocalDateTime.now()));
            return null;
        }).when(paymentValidator).validateAmount(any(), anyList());

        assertThrows(MultiValidationException.class, () -> paymentService.accountToAccount(1, request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldCallProcessorForEachPendingPaymentOnSinglePage() {
        PaymentEntity payment1 = new PaymentEntity();
        payment1.setId(1);
        PaymentEntity payment2 = new PaymentEntity();
        payment2.setId(2);

        Page<PaymentEntity> singlePage = new PageImpl<>(List.of(payment1, payment2));

        when(paymentRepository.findAllByStatusOrderByCreatedAtAsc(
                eq(PaymentStatus.PENDING), any(Pageable.class)))
                .thenReturn(singlePage);

        paymentService.processPayments();

        verify(paymentProcessor).process(1);
        verify(paymentProcessor).process(2);
    }

    @Test
    void shouldNotCallProcessorWhenNoPendingPaymentsExist() {
        when(paymentRepository.findAllByStatusOrderByCreatedAtAsc(
                eq(PaymentStatus.PENDING), any(Pageable.class)))
                .thenReturn(Page.empty());

        paymentService.processPayments();

        verify(paymentProcessor, never()).process(any());
    }

    @Test
    void shouldReturnPaymentResponseWhenPaymentFoundByIdAndCustomerId() {
        PaymentResponse expected = new PaymentResponse();
        expected.setId(1);

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(paymentRepository.findByIdAndCustomerId(1, 1)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.getPaymentById(1, 1);

        assertEquals(expected.getId(), actual.getId());
    }

    @Test
    void shouldThrowPaymentNotFoundExceptionWhenPaymentDoesNotExist() {
        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(paymentRepository.findByIdAndCustomerId(99, 1)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(1, 99));
    }

    @Test
    void shouldThrowPaymentNotFoundExceptionWhenIdempotencyKeyExistsButPaymentNotFound() {
        CardToCardRequest request = new CardToCardRequest();
        request.setFromPan("4000000000000001");
        request.setToPan("4000000000000002");
        request.setAmount(BigDecimal.TEN);

        Class<PaymentNotFoundException> expected = PaymentNotFoundException.class;

        when(paymentRepository.existsByIdempotencyKey(anyString())).thenReturn(true);
        when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        assertThrows(expected, () -> paymentService.cardToCard(1, request));
    }
}