package az.bank.paymentsystem.service;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.PaymentRepository;
import az.bank.paymentsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityFinderService {
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final PaymentRepository paymentRepository;

    public CustomerEntity findActiveCustomer(Integer customerId) {
        return customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    public CardEntity findActiveCard(Integer cardId) {
        return cardRepository.findByIdAndIsVisibleTrue(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    public CurrentAccountEntity findActiveAccount(Integer accountId) {
        return currentAccountRepository.findByIdAndIsVisibleTrue(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }









}
