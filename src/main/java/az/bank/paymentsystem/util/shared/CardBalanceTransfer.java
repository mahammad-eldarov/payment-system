package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardBalanceTransfer {

    private final CardRepository cardRepository;
    private final TransactionCreator transactionCreator;

    public String transfer(CardEntity card) {
        BigDecimal balance = card.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return "Card was successfully deleted.";
        }

        Integer customerId = card.getCustomer().getId();

        CardEntity otherCard = cardRepository
                .findFirstByCustomerIdAndIsVisibleTrueAndIdNot(customerId, card.getId()).orElse(null);
        if (otherCard != null) {
            return transferToCard(card, otherCard, balance);
        }

        card.setBalance(BigDecimal.ZERO);
        return "Your remaining balance of " + balance + " AZN can be collected by visiting your nearest branch.";
    }

//    private String transferToCard(CardEntity card, CardEntity otherCard, BigDecimal balance) {
//        otherCard.setBalance(otherCard.getBalance().add(balance));
//        card.setBalance(BigDecimal.ZERO);
//        cardRepository.save(otherCard);
//        // transaction yarat
//        transactionCreator.createBalanceTransfer(card, otherCard, balance,
//                "Balance transferred from expired/closed card ending in "
//                        + card.getPan().substring(card.getPan().length() - 4));
//        return "Your remaining balance of " + balance + " AZN has been transferred to your card ending in "
//                + otherCard.getPan().substring(otherCard.getPan().length() - 4) + ".";
//    }
    private String transferToCard(CardEntity card, CardEntity otherCard, BigDecimal balance) {
        otherCard.setBalance(otherCard.getBalance().add(balance));
        card.setBalance(BigDecimal.ZERO);
        cardRepository.save(otherCard);

        String debitDescription = "Your card ending in " + card.getPan().substring(card.getPan().length() - 4)
                + " was expired or closed. Balance of " + balance + " AZN has been transferred to your card ending in "
                + otherCard.getPan().substring(otherCard.getPan().length() - 4) + ".";

        String creditDescription = "Balance transferred from card ending in " + card.getPan().substring(card.getPan().length() - 4)
                + " to card ending in " + otherCard.getPan().substring(otherCard.getPan().length() - 4);

//                "Balance of " + balance + " AZN transferred from your expired/closed card ending in "
//                + card.getPan().substring(card.getPan().length() - 4) + ".";

        transactionCreator.createBalanceTransfer(card, otherCard, balance, debitDescription, creditDescription);


        return "Your remaining balance of " + balance + " AZN has been transferred to your card ending in "
                + otherCard.getPan().substring(otherCard.getPan().length() - 4) + ".";
    }

}
