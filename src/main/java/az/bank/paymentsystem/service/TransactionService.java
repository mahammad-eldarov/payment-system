package az.bank.paymentsystem.service;

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

//    private final TransactionRepository transactionRepository;
//    private final CardRepository cardRepository;
//    private final CurrentAccountRepository currentAccountRepository;
    private final TransactionMapper transactionMapper;
//    private final PaymentRepository paymentRepository;
    private final EntityFinderService entityFinderService;


    // BURDA DƏYİŞİKLİKLƏR OLACAQ - Description düz deyil, full data gəlir olmaz!


    // card üçün son 100 tranzaksiyanı tapıb gətirir

//    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByCardId(Integer cardId, int page) {
//        cardRepository.findByIdAndIsVisibleTrue(cardId)
//                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        entityFinderService.findActiveCurrentAccount(cardId);

        Pageable pageable = buildPageable(page);
//        Page<TransactionEntity> transactions = transactionRepository
//                .findByFromCardIdOrToCardId(cardId, cardId, pageable);
        Page<TransactionEntity> transactions = entityFinderService.findCardToCard(cardId, cardId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException("No transactions found for this card.");
        return transactions.map(transactionMapper::toResponse);
    }

    // current account üçün son 100 tranzaksiyanı tapıb gətirir

    public Page<TransactionResponse> getTransactionsByAccountId(Integer accountId, int page) {
//        currentAccountRepository.findByIdAndIsVisibleTrue(accountId)
//                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        entityFinderService.findActiveCurrentAccount(accountId);
        Pageable pageable = buildPageable(page);
//        Page<TransactionEntity> transactions = transactionRepository
//                .findByFromAccountIdOrToAccountId(accountId, accountId, pageable);
        Page<TransactionEntity> transactions = entityFinderService.findAccountToAccount(accountId, accountId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException("No transactions found for this account.");
        return transactions.map(transactionMapper::toResponse);
    }

    // payment id-yə görə tranzaksiyalarını tapıb gətirir

    public Page<TransactionResponse> getTransactionsByPaymentId(Integer paymentId, int page) {
//        paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
        entityFinderService.findPaymentById(paymentId);
        Pageable pageable = buildPageable(page);
//        Page<TransactionEntity> transactions = transactionRepository
//                .findAllByPaymentId(paymentId, pageable);
        Page<TransactionEntity> transactions = entityFinderService
                .findAllPayment(paymentId, pageable);

        if (transactions.isEmpty()) throw new EmptyListException("No transactions found for this payment.");
        return transactions.map(transactionMapper::toResponse);
    }

    // RESPONSE
//    public TransactionResponse toResponse(TransactionEntity transaction) {
//        return transactionMapper.toResponse(transaction);
//    }

    private Pageable buildPageable(int page) {
        if (page < 1) throw new PageRequestException("Page number must be at least 1");
        return PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
    }


}