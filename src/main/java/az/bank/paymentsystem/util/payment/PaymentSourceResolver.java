package az.bank.paymentsystem.util.payment;

import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
//import az.bank.paymentsystem.service.EntityFinderService;
import az.bank.paymentsystem.util.shared.CurrencyConverter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.config.BankConfig;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSourceResolver {
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final BankConfig bankConfig;
    private final CurrencyConverter currencyConverter;
//    private final EntityFinderService entityFinderService;


    // FROM CHECKS
    public void fromCheckCard(PaymentEntity payment, Integer customerId,
                               String fromPan, List<ExceptionResponse> errors) {
        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(fromPan).orElse(null);
//        CardEntity card = entityFinderService.findOptionalCardPanVisibleTrue(fromPan).orElse(null);
        if (card == null) {
            errors.add(new ExceptionResponse(404, "Source card not found", LocalDateTime.now()));
            return;
        }
        if (!card.getCustomer().getId().equals(customerId)) {
            errors.add(new ExceptionResponse(403, "Source card does not belong to this customer", LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, "Your card is suspended due to suspicious activity.", LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            errors.add(new ExceptionResponse(400, "Card is expired", LocalDateTime.now()));
            return;
        }
        if (payment.getCustomer().getStatus() == CustomerStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, "Your profile is suspended due to suspicious activity.", LocalDateTime.now()));
            return;
        }

        boolean cardHasSufficientBalance = card.getBalance().compareTo(payment.getAmount()) >= 0;
        boolean cardAboveMinBalance = card.getBalance().compareTo(bankConfig.getCard().getMinBalance()) >= 0;

        if (cardHasSufficientBalance && cardAboveMinBalance) {
            payment.setFromCard(card);
            payment.setCurrency(card.getCurrency());
        } else {
            fallbackToAccount(payment, customerId, card.getCurrency(), errors);
        }
    }

    public void fromCheckAccount(PaymentEntity payment, Integer customerId,
                                  String fromAccountNumber, List<ExceptionResponse> errors) {
        CurrentAccountEntity account = currentAccountRepository
                .findByAccountNumberAndIsVisibleTrue(fromAccountNumber).orElse(null);
//        CurrentAccountEntity account = entityFinderService.findCurrentAccountNumberVisibleTrue(fromAccountNumber).orElse(null);
        if (account == null) {
            errors.add(new ExceptionResponse(404, "Source current account not found", LocalDateTime.now()));
            return;
        }
        if (!account.getCustomer().getId().equals(customerId)) {
            errors.add(new ExceptionResponse(403, "Source current account does not belong to this customer", LocalDateTime.now()));
            return;
        }
        if (account.getStatus() == CurrentAccountStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, "Your current account is suspended due to suspicious activity.", LocalDateTime.now()));
            return;
        }
        if (payment.getCustomer().getStatus() == CustomerStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, "Your profile is suspended due to suspicious activity.", LocalDateTime.now()));
            return;
        }
        if (account.getStatus() == CurrentAccountStatus.EXPIRED) {
            errors.add(new ExceptionResponse(403, "Your current account is expired.", LocalDateTime.now()));
            return;
        }
        if (account.getStatus() == CurrentAccountStatus.CLOSED) {
            errors.add(new ExceptionResponse(403, "Your current account is closed.", LocalDateTime.now()));
            return;
        }

        BigDecimal minBalanceInAccountCurrency = currencyConverter.convertMinBalance(
                bankConfig.getAccount().getMinBalance(),
                bankConfig.getAccount().getMinBalanceCurrency(),
                account.getCurrency());

        boolean accountHasSufficientBalance = account.getBalance().compareTo(payment.getAmount()) >= 0;
        boolean accountAboveMinBalance = account.getBalance().compareTo(minBalanceInAccountCurrency) >= 0;

        if (accountHasSufficientBalance && accountAboveMinBalance) {
            payment.setFromAccount(account);
            payment.setCurrency(account.getCurrency());
        } else {
            fallbackToCard(payment, customerId, errors);
        }
    }

    private void fallbackToAccount(PaymentEntity payment, Integer customerId,
                                   Currency cardCurrency, List<ExceptionResponse> errors) {
        CurrentAccountEntity account = currentAccountRepository
                .findSufficientAccount(customerId, payment.getAmount()).orElse(null);
//        CurrentAccountEntity account = entityFinderService
//                .findOptionalCurrentAccountBalance(customerId, payment.getAmount()).orElse(null);
        if (account == null) {
            errors.add(new ExceptionResponse(400, "Insufficient balance in both card and current account", LocalDateTime.now()));
            return;
        }

        BigDecimal minBalanceInAccountCurrency = currencyConverter.convertMinBalance(
                bankConfig.getAccount().getMinBalance(),
                bankConfig.getAccount().getMinBalanceCurrency(),
                account.getCurrency());

        BigDecimal amountInAccountCurrency = currencyConverter.convert(
                payment.getAmount(), cardCurrency, account.getCurrency());

        if (account.getBalance().compareTo(amountInAccountCurrency) < 0 ||
                account.getBalance().compareTo(minBalanceInAccountCurrency) < 0) {
            errors.add(new ExceptionResponse(400, "Insufficient balance in both card and current account", LocalDateTime.now()));
            return;
        }

        payment.setFromAccount(account);
        payment.setFromType(PaymentSourceType.CURRENT_ACCOUNT);
        payment.setCurrency(account.getCurrency());
    }

    private void fallbackToCard(PaymentEntity payment, Integer customerId,
                                List<ExceptionResponse> errors) {
        CardEntity card = cardRepository.findSufficientCard(customerId, payment.getAmount()).orElse(null);
//        CardEntity card = entityFinderService.findOptionalCardBalance(customerId, payment.getAmount()).orElse(null);
        if (card == null) {
            errors.add(new ExceptionResponse(400, "Insufficient balance in both current account and card", LocalDateTime.now()));
            return;
        }
        payment.setFromCard(card);
        payment.setFromType(PaymentSourceType.CARD);
        payment.setCurrency(card.getCurrency());
    }

    // TO CHECKS
    public void toCheckCard(PaymentEntity payment, String toPan, List<ExceptionResponse> errors) {
        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(toPan).orElse(null);
//        CardEntity card = entityFinderService.findOptionalCardPanVisibleTrue(toPan).orElse(null);
        if (card == null) {
            errors.add(new ExceptionResponse(404, "Destination card not found", LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            errors.add(new ExceptionResponse(400, "Destination card is expired", LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.CLOSED) {
            errors.add(new ExceptionResponse(403, "Destination card is closed.", LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, "Destination card is suspended due to suspicious activity.", LocalDateTime.now()));
            return;
        }
        payment.setToCard(card);
    }

    public void toCheckAccount(PaymentEntity payment, String toAccountNumber, List<ExceptionResponse> errors) {
        CurrentAccountEntity account = currentAccountRepository
                .findByAccountNumberAndIsVisibleTrue(toAccountNumber).orElse(null);
//        CurrentAccountEntity account = entityFinderService.findCurrentAccountNumberVisibleTrue(toAccountNumber).orElse(null);
        if (account == null) {
            errors.add(new ExceptionResponse(404, "Destination current account not found", LocalDateTime.now()));
            return;
        }
        if (account.getStatus() == CurrentAccountStatus.EXPIRED) {
            errors.add(new ExceptionResponse(403, "Destination current account is expired.", LocalDateTime.now()));
            return;
        }
        if (account.getStatus() == CurrentAccountStatus.CLOSED) {
            errors.add(new ExceptionResponse(403, "Destination current account is closed.", LocalDateTime.now()));
            return;
        }
        if (account.getStatus() == CurrentAccountStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, "Destination current account is suspended due to suspicious activity.", LocalDateTime.now()));
            return;
        }
        payment.setToAccount(account);
    }




}
