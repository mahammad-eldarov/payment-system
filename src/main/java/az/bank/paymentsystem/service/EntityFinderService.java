package az.bank.paymentsystem.service;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.entity.TransactionEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.PaymentNotFoundException;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.PaymentRepository;
import az.bank.paymentsystem.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityFinderService {
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final PaymentRepository paymentRepository;

//    public CustomerEntity findActiveCustomer(Integer customerId) {
//        return customerRepository.findByIdAndIsVisibleTrue(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//    }
public CustomerEntity findActiveCustomer(Integer customerId) {
    return customerRepository.findByIdAndIsVisibleTrue(customerId).orElseThrow(() -> new MultiValidationException(List.of(
            new ExceptionResponse(404, "Customer not found", LocalDateTime.now()))));
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

    public Boolean findExistingPinVisibleTrue(String pin){
        return customerRepository.existsByPinAndIsVisibleTrue(pin);
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

    public Optional<CardEntity> findOptionalCardPanVisibleTrue(String pan) {
        return cardRepository.findByPanAndIsVisibleTrue(pan);
    }

    public Optional<CardEntity> findOptionalCardBalance(Integer customerId, BigDecimal amount) {
        return cardRepository.findSufficientCard(customerId, amount);
    }


    public Boolean findCustomerExistingCardStatus(Integer customerId){
       return cardRepository.existsByCustomerIdAndStatusIn(
               customerId, List.of(CardStatus.SUSPICIOUS, CardStatus.LOST, CardStatus.STOLEN));
    }

    public Integer countCustomerCardVisibleTrue(Integer customerId){
        return cardRepository.countByCustomerIdAndIsVisibleTrue(customerId);
    }



    public Optional<CurrentAccountEntity> findCurrentAccountNumberVisibleTrue(String accountNumber){
        return currentAccountRepository.findByAccountNumberAndIsVisibleTrue(accountNumber);
    }

    public Optional<CurrentAccountEntity> findOptionalCurrentAccountBalance(Integer customerId, BigDecimal amount) {
        return currentAccountRepository.findSufficientAccount(customerId, amount);
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

    public Boolean findExistingAccountNumber(String accountNumber){
        return currentAccountRepository.existsByAccountNumber(accountNumber);
    }





    public PaymentEntity findPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
    }

    public PaymentEntity savePayment(PaymentEntity payment) {
        return paymentRepository.save(payment);
    }

    public List<PaymentEntity> findAllPaymentStatus() {
        return paymentRepository.findAllByStatus(PaymentStatus.PENDING);
    }

    public Boolean findExistingScheduledCustomerPaymentStatus(Integer customerId) {
        return paymentRepository.existsByCustomerIdAndScheduledDateAndStatus(
                customerId, LocalDate.now(), PaymentStatus.PENDING);

    }




//    public Page<TransactionEntity> findCardToCard(Integer fromCardId, Integer ToCardId, Pageable page){
//        return transactionRepository.findByFromCardIdOrToCardId(fromCardId,ToCardId, page);
//    }
    public Page<TransactionEntity> findCardToCard(Integer fromCardId, Integer toCardId, Pageable pageable) {
        return transactionRepository.findByFromCardIdOrToCardId(fromCardId, toCardId, pageable);
    }

    public Page<TransactionEntity> findAccountToAccount(Integer fromAccountId, Integer toAccountId, Pageable pageable) {
        return transactionRepository.findByFromCardIdOrToCardId(fromAccountId, toAccountId, pageable);
    }

    public Page<TransactionEntity> findAllPayment(Integer paymentId, Pageable pageable) {
        return transactionRepository.findAllByPaymentId(paymentId, pageable);
    }




















}
