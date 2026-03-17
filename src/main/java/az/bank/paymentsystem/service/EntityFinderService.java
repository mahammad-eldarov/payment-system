package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.PaymentRepository;
import az.bank.paymentsystem.repository.TransactionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
        return customerRepository.findByIdAndIsVisibleTrue(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    public void saveCustomer(CustomerEntity customer) {
        customerRepository.save(customer);
    }

    public List<CustomerEntity> findAllCustomerVisibleTrue(){
        return customerRepository.findAllByIsVisibleTrue();
    }

    public Optional<CustomerEntity> findCustomerIdVisibleFalse(Integer id){
        return customerRepository.findByIdAndIsVisibleFalse(id);
    }

    public Optional<CustomerEntity> findCustomerIdVisibleTrue(Integer id){
        return customerRepository.findByIdAndIsVisibleTrue(id);
    }

    public List<CustomerEntity> findCustomerStatusVisibleTrue(CustomerStatus status){
        return customerRepository.findByStatusAndIsVisibleTrue(status);
    }

    public List<CustomerEntity> findCustomerStatusVisibleFalse(CustomerStatus status){
        return customerRepository.findByStatusAndIsVisibleFalse(status);
    }



    public CardEntity findActiveCard(Integer cardId) {
        return cardRepository.findByIdAndIsVisibleTrue(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    public void saveCard(CardEntity card) {
        cardRepository.save(card);
    }

    public void saveAllExpiredCards(List<CardEntity> expiredCards) {
        cardRepository.saveAll(expiredCards);
    }

    public List<CardEntity> findAllCardNotReachExpiryDate(){
        return cardRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CardStatus.EXPIRED);
    }

    public List<CardEntity> findCardStatusVisibleTrue(CardStatus status){
        return cardRepository.findByStatusAndIsVisibleTrue(status);
    }

    public List<CardEntity> findCardsCustomerId(Integer customerId){
        return cardRepository.findCardsByCustomerId(customerId);
    }

    public CardEntity findCardPanVisibleTrue (String pan){
        return cardRepository.findByPanAndIsVisibleTrue(pan).orElseThrow(() -> new CardNotFoundException("Card not found"));
    }



    public CurrentAccountEntity findActiveCurrentAccount(Integer accountId) {
        return currentAccountRepository.findByIdAndIsVisibleTrue(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }
    public CurrentAccountEntity findActiveCurrentAccountByNumber(String accountNumber) {
        return currentAccountRepository.findByAccountNumberAndIsVisibleTrue(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Current account not found"));
    }

    public void saveCurrentAccount(CurrentAccountEntity account) {
        currentAccountRepository.save(account);
    }

    public void saveAllExpiredCurrentAccounts(List<CurrentAccountEntity> expiredCurrentAccounts) {
        currentAccountRepository.saveAll(expiredCurrentAccounts);
    }

    public List<CurrentAccountEntity> findAllCurrentAccountNotReachExpiryDate(){
        return currentAccountRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CurrentAccountStatus.EXPIRED);
    }

    public List<CurrentAccountEntity> findCurrentAccountStatusVisibleTrue(CurrentAccountStatus status){
        return currentAccountRepository.findByStatusAndIsVisibleTrue(status);
    }

    public List<CurrentAccountEntity> findCurrentAccountsCustomerId(Integer customerId){
        return currentAccountRepository.findByCustomerIdAndIsVisibleTrue(customerId);
    }

    public Integer countCurrentAccountVisibleTrue(Integer customerId){
        return currentAccountRepository.countByCustomerIdAndIsVisibleTrue(customerId);
    }
















}
