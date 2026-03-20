package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.service.NotificationService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardBalanceTransfer {

    private final CardRepository cardRepository;
    private final TransactionCreator transactionCreator;
    private final NotificationService notificationService;

    public String transfer(CardEntity card) {
        BigDecimal balance = card.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return "Card was successfully deleted.";
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            String message =  "Your balance of " + balance + " " + card.getCurrency()
                    + " has been frozen due to suspicious activity on your card.";
            notificationService.send(card.getCustomer(), message);
            return message;
        }

        Integer customerId = card.getCustomer().getId();
        String lastFour = card.getPan().substring(card.getPan().length() - 4);
        String reason = getStatusReason(card.getStatus());

//        Integer customerId = card.getCustomer().getId();

        CardEntity otherCard = cardRepository
                .findFirstByCustomerIdAndIsVisibleTrueAndIdNot(customerId, card.getId()).orElse(null);
        if (otherCard != null && isTransferableCard(otherCard)) {
            return transferToCard(card, otherCard, balance, reason);
        }
        String message = "Your card ending in " + lastFour
                + " has been " + reason + ". Your remaining balance of "
                + balance + " " + card.getCurrency()
                + " can be collected by visiting your nearest branch.";
        notificationService.send(card.getCustomer(), message);
        return message;

    }

    private String getStatusReason(CardStatus status) {
        return switch (status) {
            case BLOCKED -> "blocked";
            case EXPIRED -> "expired";
            case CLOSED -> "closed";
            case LOST -> "reported as lost";
            case STOLEN -> "reported as stolen";
            case SUSPICIOUS -> "suspicious";
            default -> "deactivated";

        };
    }

    private boolean isTransferableCard(CardEntity card) {
        return card.getStatus() != CardStatus.SUSPICIOUS
                && card.getStatus() != CardStatus.CLOSED
                && card.getStatus() != CardStatus.LOST
                && card.getStatus() != CardStatus.STOLEN
                && card.getStatus() != CardStatus.EXPIRED;
    }

    private String transferToCard(CardEntity card, CardEntity otherCard, BigDecimal balance, String reason) {
        otherCard.setBalance(otherCard.getBalance().add(balance));
        card.setBalance(BigDecimal.ZERO);
        cardRepository.save(otherCard);

        String lastFour = card.getPan().substring(card.getPan().length() - 4);
        String otherLastFour = otherCard.getPan().substring(otherCard.getPan().length() - 4);

        String debitDescription = "Your card ending in " + lastFour
                + " was " + reason + ". Balance of " + balance + " " + card.getCurrency()
                + " has been transferred to your card ending in " + otherLastFour + ".";

        String creditDescription = "Balance transferred from card ending in "
                + lastFour + " to card ending in " + otherLastFour;

        transactionCreator.createBalanceTransfer(card, otherCard, balance, debitDescription, creditDescription);


        return "Your remaining balance of " + balance + " " + card.getCurrency()
                + " has been transferred to your card ending in " + otherLastFour + ".";
    }




//    public String transfer(CardEntity card) {
//        BigDecimal balance = card.getBalance();
//
//        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
//            return "Card was successfully deleted.";
//        }
//        if (card.getStatus() == CardStatus.SUSPICIOUS) {
//            String message =  "Your balance of " + balance + " " + card.getCurrency()
//                    + " has been frozen due to suspicious activity on your card.";
//            notificationService.send(card.getCustomer(), message);
//            return message;
//        }
//
//        Integer customerId = card.getCustomer().getId();
//
//        CardEntity otherCard = cardRepository
//                .findFirstByCustomerIdAndIsVisibleTrueAndIdNot(customerId, card.getId()).orElse(null);
//        if (otherCard != null && isTransferableCard(otherCard)) {
//            return transferToCard(card, otherCard, balance);
//        }
//        String message = "Your card ending in " + card.getPan().substring(card.getPan().length() - 4)
//                + " has expired. " + "Your remaining balance of " + balance + card.getCurrency() +" can be collected by visiting your nearest branch.";
////        String message = "Your card ending in " + card.getPan().substring(card.getPan().length() - 4)
////                + " has been successfully closed. ";
////        String message =  "Your remaining balance of " + balance + card.getCurrency() +" can be collected by visiting your nearest branch.";
//        notificationService.send(card.getCustomer(), message);
//        return message;
//    }

}
