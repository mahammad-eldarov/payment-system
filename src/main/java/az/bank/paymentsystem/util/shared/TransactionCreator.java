package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.entity.TransactionEntity;
import az.bank.paymentsystem.enums.TransactionStatus;
import az.bank.paymentsystem.enums.TransactionType;
import az.bank.paymentsystem.repository.TransactionRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionCreator {

    private final TransactionRepository transactionRepository;
    private final MessageSource messageSource;

    public void create(PaymentEntity payment, TransactionStatus status) {
        transactionRepository.save(buildTransaction(payment, status, TransactionType.DEBIT));
        transactionRepository.save(buildTransaction(payment, status, TransactionType.CREDIT));

    }

    private TransactionEntity buildTransaction(PaymentEntity payment, TransactionStatus status, TransactionType type) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setPayment(payment);
        transaction.setCustomer(payment.getCustomer());
        transaction.setAmount(payment.getAmount());
        transaction.setCurrency(payment.getCurrency());
        transaction.setStatus(status);
        transaction.setTransactionType(type);
        transaction.setDescription(buildDescription(payment));

        if (type == TransactionType.CREDIT) {
            transaction.setFromCard(payment.getFromCard());
            transaction.setFromAccount(payment.getFromAccount());
        } else {
            transaction.setToCard(payment.getToCard());
            transaction.setToAccount(payment.getToAccount());
            transaction.setToExternalParty(payment.getToExternalParty());
        }

        transaction.setCreatedAt(Instant.now());
        return transaction;
    }

    private String buildDescription(PaymentEntity payment) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage("transactionCreator.buildDescription",new Object[]{payment.getAmount(), payment.getCurrency(), payment.getFromType(), payment.getToType()}, locale);
    }

    public void createBalanceTransfer(CardEntity fromCard, CardEntity toCard,
                                      BigDecimal amount, String debitDescription, String creditDescription) {
        TransactionEntity credit = new TransactionEntity();
        credit.setFromCard(fromCard);
        credit.setAmount(amount);
        credit.setCurrency(fromCard.getCurrency());
        credit.setStatus(TransactionStatus.SUCCESS);
        credit.setTransactionType(TransactionType.CREDIT);
        credit.setDescription(creditDescription);
        credit.setCustomer(fromCard.getCustomer());
        credit.setCreatedAt(Instant.now());

        TransactionEntity debit = new TransactionEntity();
        debit.setToCard(toCard);
        debit.setAmount(amount);
        debit.setCurrency(fromCard.getCurrency());
        debit.setStatus(TransactionStatus.SUCCESS);
        debit.setTransactionType(TransactionType.DEBIT);
        debit.setDescription(debitDescription);
        debit.setCustomer(fromCard.getCustomer());
        debit.setCreatedAt(Instant.now());

        transactionRepository.saveAll(List.of(debit, credit));
    }

}
