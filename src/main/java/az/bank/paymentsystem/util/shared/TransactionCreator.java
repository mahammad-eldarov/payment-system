package az.bank.paymentsystem.util.shared;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.entity.TransactionEntity;
import az.bank.paymentsystem.enums.TransactionStatus;
import az.bank.paymentsystem.enums.TransactionType;
import az.bank.paymentsystem.repository.TransactionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionCreator {

    private final TransactionRepository transactionRepository;

    public void create(PaymentEntity payment, TransactionStatus status) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setPayment(payment);
        transaction.setCustomer(payment.getCustomer());
        transaction.setAmount(payment.getAmount());
        transaction.setCurrency(payment.getCurrency());
        transaction.setStatus(status);
        transaction.setFromCard(payment.getFromCard());
        transaction.setFromAccount(payment.getFromAccount());
        transaction.setToCard(payment.getToCard());
        transaction.setToAccount(payment.getToAccount());
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setDescription(String.format("Transfer of %s %s from %s to %s",
                payment.getAmount(), payment.getCurrency(),
                payment.getFromType(), payment.getToType()));
        transaction.setCreatedAt(Instant.now());
        transactionRepository.save(transaction);
    }
}
