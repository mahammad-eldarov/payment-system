package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.service.NotificationService;
import java.math.BigDecimal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardBalanceTransfer {

    private final CardRepository cardRepository;
    private final TransactionCreator transactionCreator;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    public void transfer(CardEntity card, Locale locale) {
        BigDecimal balance = card.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            messageSource.getMessage("cardBalanceTransfer.transfer.cardDeleted", null, locale);
            return;
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            String message = messageSource.getMessage("cardBalanceTransfer.transfer.cardSuspiciousActivity", new Object[]{balance, card.getCurrency()},locale);
            notificationService.send(card.getCustomer(), message);
            return;
        }

        Integer customerId = card.getCustomer().getId();
        String lastFour = card.getPan().substring(card.getPan().length() - 4);
        String reason = getStatusReason(card.getStatus(),locale);

        CardEntity otherCard = cardRepository
                .findFirstByCustomerIdAndIsVisibleTrueAndIdNot(customerId, card.getId()).orElse(null);
        if (otherCard != null && isTransferableCard(otherCard)) {
            transferToCard(card, otherCard, balance, reason, locale);
            return;
        }
        String message = messageSource.getMessage("cardBalanceTransfer.transfer.visitingBranch",new Object[]{lastFour, reason, balance, card.getCurrency()},locale);
        notificationService.send(card.getCustomer(), message);

    }

    private String getStatusReason(CardStatus status, Locale locale) {
        return switch (status) {
            case BLOCKED -> messageSource.getMessage("cardBalanceTransfer.getStatusReason.blocked",null,locale);
            case EXPIRED -> messageSource.getMessage("cardBalanceTransfer.getStatusReason.expired",null,locale);
            case CLOSED -> messageSource.getMessage("cardBalanceTransfer.getStatusReason.closed",null,locale);
            case LOST -> messageSource.getMessage("cardBalanceTransfer.getStatusReason.lost",null,locale);
            case STOLEN -> messageSource.getMessage("cardBalanceTransfer.getStatusReason.stolen",null,locale);
            case SUSPICIOUS -> messageSource.getMessage("cardBalanceTransfer.getStatusReason.suspicious",null,locale);
            default -> messageSource.getMessage("cardBalanceTransfer.getStatusReason.deactivated",null,locale);

        };
    }

    private boolean isTransferableCard(CardEntity card) {
        return card.getStatus() != CardStatus.SUSPICIOUS
                && card.getStatus() != CardStatus.CLOSED
                && card.getStatus() != CardStatus.LOST
                && card.getStatus() != CardStatus.STOLEN
                && card.getStatus() != CardStatus.EXPIRED;
    }

    private void transferToCard(CardEntity card, CardEntity otherCard, BigDecimal balance, String reason, Locale locale) {
        otherCard.setBalance(otherCard.getBalance().add(balance));
        card.setBalance(BigDecimal.ZERO);
        cardRepository.save(otherCard);

        String lastFour = card.getPan().substring(card.getPan().length() - 4);
        String otherLastFour = otherCard.getPan().substring(otherCard.getPan().length() - 4);

        String debitDescription = messageSource.getMessage("cardBalanceTransfer.transferToCard.balanceTransferred.regexp", new Object[]{lastFour, reason, balance, card.getCurrency(), otherLastFour},locale);

        String creditDescription = messageSource.getMessage("cardBalanceTransfer.transferToCard.balanceTransferred",new Object[]{lastFour, otherLastFour},locale);

        transactionCreator.createBalanceTransfer(card, otherCard, balance, debitDescription, creditDescription);


        messageSource.getMessage("cardBalanceTransfer.transferToCard.remainingBalance.regexp", new Object[]{balance, card.getCurrency(), otherLastFour}, locale);
    }

}
