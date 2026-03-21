package az.bank.paymentsystem.service;

import az.bank.paymentsystem.exception.CustomerNotFoundException;
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

    public Page<TransactionResponse> getTransactionsByCardId(Integer cardId, int page) {
        cardRepository.findByIdAndIsVisibleTrue(cardId)
                .orElseThrow(() -> new CardNotFoundException("transactionService.getTransactionsByCardId.cardNotFound"));

        Pageable pageable = buildPageable(page);
        Page<TransactionEntity> transactions = transactionRepository
                .findByFromCardIdOrToCardId(cardId, cardId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException("transactionService.getTransactionsByCardId.cardNoTransaction");
        return transactions.map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByAccountId(Integer accountId, int page) {
        currentAccountRepository.findByIdAndIsVisibleTrue(accountId)
                .orElseThrow(() -> new AccountNotFoundException("transactionService.getTransactionsByAccountId.currentAccountNotFound"));
        Pageable pageable = buildPageable(page);
        Page<TransactionEntity> transactions = transactionRepository
                .findByFromAccountIdOrToAccountId(accountId, accountId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException("transactionService.getTransactionsByAccountId.accountNoTransaction");
        return transactions.map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByPaymentId(Integer paymentId, int page) {
        paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("transactionService.getTransactionsByPaymentId.paymentNotFound"));
        Pageable pageable = buildPageable(page);
        Page<TransactionEntity> transactions = transactionRepository
                .findAllByPaymentId(paymentId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException("transactionService.getTransactionsByPaymentId.paymentNoTransaction");
        return transactions.map(transactionMapper::toResponse);
    }

    private Pageable buildPageable(int page) {
        if (page < 1) throw new PageRequestException("transactionService.buildPageable.pageNumber");
        return PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
    }


}