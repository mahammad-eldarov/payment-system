package az.bank.paymentsystem.service;

import az.bank.paymentsystem.exception.CustomerNotFoundException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.TransactionResponse;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final TransactionMapper transactionMapper;
    private final PaymentRepository paymentRepository;
    private final MessageSource messageSource;

    public Page<TransactionResponse> getTransactionsByCardId(Integer cardId, int page) {
        Locale locale = LocaleContextHolder.getLocale();
        cardRepository.findByIdAndIsVisibleTrue(cardId)
                .orElseThrow(() -> new CardNotFoundException(messageSource.getMessage("transactionService.getTransactionsByCardId.cardNotFound", null, locale)));

        Pageable pageable = buildPageable(page);
        Page<TransactionEntity> transactions = transactionRepository
                .findByFromCardIdOrToCardId(cardId, cardId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException(messageSource.getMessage("transactionService.getTransactionsByCardId.cardNoTransaction", null, locale));
        return transactions.map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByAccountId(Integer accountId, int page) {
        Locale locale = LocaleContextHolder.getLocale();

        currentAccountRepository.findByIdAndIsVisibleTrue(accountId)
                .orElseThrow(() -> new AccountNotFoundException(messageSource.getMessage("transactionService.getTransactionsByAccountId.currentAccountNotFound", null, locale)));
        Pageable pageable = buildPageable(page);
        Page<TransactionEntity> transactions = transactionRepository
                .findByFromAccountIdOrToAccountId(accountId, accountId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException(messageSource.getMessage("transactionService.getTransactionsByAccountId.accountNoTransaction", null, locale));
        return transactions.map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByPaymentId(Integer paymentId, int page) {
        Locale locale = LocaleContextHolder.getLocale();

        paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(messageSource.getMessage("transactionService.getTransactionsByPaymentId.paymentNotFound", null, locale)));
        Pageable pageable = buildPageable(page);
        Page<TransactionEntity> transactions = transactionRepository
                .findAllByPaymentId(paymentId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException(messageSource.getMessage("transactionService.getTransactionsByPaymentId.paymentNoTransaction", null, locale));
        return transactions.map(transactionMapper::toResponse);
    }

    private Pageable buildPageable(int page) {
        Locale locale = LocaleContextHolder.getLocale();

        if (page < 1) throw new PageRequestException(messageSource.getMessage("transactionService.buildPageable.pageNumber", null, locale));
        return PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
    }

}