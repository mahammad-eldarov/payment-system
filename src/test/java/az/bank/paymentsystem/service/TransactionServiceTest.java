package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.entity.TransactionEntity;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.exception.PaymentNotFoundException;
import az.bank.paymentsystem.mapper.TransactionMapper;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.PaymentRepository;
import az.bank.paymentsystem.repository.TransactionRepository;
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
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CardRepository cardRepository;
    @Mock private CurrentAccountRepository currentAccountRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private PaymentRepository paymentRepository;
    @Mock private MessageSource messageSource;

    @InjectMocks
    private TransactionService transactionService;

    private CardEntity card;
    private CurrentAccountEntity account;
    private PaymentEntity payment;
    private TransactionEntity transaction;

    @BeforeEach
    void setUp() {
        card = new CardEntity();
        card.setId(1);

        account = new CurrentAccountEntity();
        account.setId(1);

        payment = new PaymentEntity();
        payment.setId(1);

        transaction = new TransactionEntity();
        transaction.setId(1);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
    }

    @Test
    void shouldReturnMappedPageWhenCardHasTransactions() {
        TransactionResponse expected = new TransactionResponse();
        expected.setId(1);

        Page<TransactionEntity> entityPage = new PageImpl<>(List.of(transaction));

        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));
        when(transactionRepository.findByFromCardIdOrToCardId(eq(1), eq(1), any(Pageable.class)))
                .thenReturn(entityPage);
        when(transactionMapper.toResponse(transaction)).thenReturn(expected);

        Page<TransactionResponse> actual = transactionService.getTransactionsByCardId(1, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenCardHasNoTransactions() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));
        when(transactionRepository.findByFromCardIdOrToCardId(eq(1), eq(1), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(expected, () -> transactionService.getTransactionsByCardId(1, 1));
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenCardDoesNotExist() {
        Class<CardNotFoundException> expected = CardNotFoundException.class;

        when(cardRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> transactionService.getTransactionsByCardId(99, 1));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetByCardId() {
        Class<PageRequestException> expected = PageRequestException.class;

        when(cardRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(card));

        assertThrows(expected, () -> transactionService.getTransactionsByCardId(1, 0));
    }

    @Test
    void shouldReturnMappedPageWhenAccountHasTransactions() {
        TransactionResponse expected = new TransactionResponse();
        expected.setId(1);

        Page<TransactionEntity> entityPage = new PageImpl<>(List.of(transaction));

        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));
        when(transactionRepository.findByFromAccountIdOrToAccountId(eq(1), eq(1), any(Pageable.class)))
                .thenReturn(entityPage);
        when(transactionMapper.toResponse(transaction)).thenReturn(expected);

        Page<TransactionResponse> actual = transactionService.getTransactionsByAccountId(1, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenAccountHasNoTransactions() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));
        when(transactionRepository.findByFromAccountIdOrToAccountId(eq(1), eq(1), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(expected, () -> transactionService.getTransactionsByAccountId(1, 1));
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        Class<AccountNotFoundException> expected = AccountNotFoundException.class;

        when(currentAccountRepository.findByIdAndIsVisibleTrue(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> transactionService.getTransactionsByAccountId(99, 1));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetByAccountId() {
        Class<PageRequestException> expected = PageRequestException.class;

        when(currentAccountRepository.findByIdAndIsVisibleTrue(1)).thenReturn(Optional.of(account));

        assertThrows(expected, () -> transactionService.getTransactionsByAccountId(1, 0));
    }

    @Test
    void shouldReturnMappedPageWhenPaymentHasTransactions() {
        TransactionResponse expected = new TransactionResponse();
        expected.setId(1);

        Page<TransactionEntity> entityPage = new PageImpl<>(List.of(transaction));

        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(transactionRepository.findAllByPaymentId(eq(1), any(Pageable.class)))
                .thenReturn(entityPage);
        when(transactionMapper.toResponse(transaction)).thenReturn(expected);

        Page<TransactionResponse> actual = transactionService.getTransactionsByPaymentId(1, 1);

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenPaymentHasNoTransactions() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(transactionRepository.findAllByPaymentId(eq(1), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(expected, () -> transactionService.getTransactionsByPaymentId(1, 1));
    }

    @Test
    void shouldThrowPaymentNotFoundExceptionWhenPaymentDoesNotExist() {
        Class<PaymentNotFoundException> expected = PaymentNotFoundException.class;

        when(paymentRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(expected, () -> transactionService.getTransactionsByPaymentId(99, 1));
    }

    @Test
    void shouldThrowPageRequestExceptionWhenPageIsLessThanOneForGetByPaymentId() {
        Class<PageRequestException> expected = PageRequestException.class;

        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));

        assertThrows(expected, () -> transactionService.getTransactionsByPaymentId(1, 0));
    }
}