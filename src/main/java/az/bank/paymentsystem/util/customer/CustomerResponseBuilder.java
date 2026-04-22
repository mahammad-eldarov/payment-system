package az.bank.paymentsystem.util.customer;

import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.exception.TinNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.base.ForbiddenException;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.mapper.TinMapper;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.dto.response.TinResponse;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.TinRepository;
import az.bank.paymentsystem.service.TransactionService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerResponseBuilder {

    private final CardRepository cardRepository;
    private final TinRepository tinRepository;
    private final TransactionService transactionService;
    private final CardMapper cardMapper;
    private final TinMapper tinMapper;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public void setCardsAndTins(CustomerResponse response, Integer customerId, CustomerEntity customer) {
        List<CardResponse> cardResponses = cardRepository.findCardsByCustomerId(customerId)
                .stream().map(cardMapper::toResponse).collect(Collectors.toList());


        List<TinResponse> tinResponses = tinRepository
                .findTinByCustomerId(customerId)
                .stream().map(tinMapper::toResponse).collect(Collectors.toList());

        response.setCardResponse(cardResponses);
        response.setCardMessage(cardMessage(cardResponses,customer));
        response.setTinResponse(tinResponses);
        response.setTinMessage(tinMessage(tinResponses,customer));
    }

    private String cardMessage(List<CardResponse> cards, CustomerEntity customer) {
        Locale locale = messageUtil.resolveLocale(customer);

        return cards.isEmpty()
                ? messageSource.getMessage("customerResponseBuilder.cardMessage.cardsEmpty", null, locale)
                : messageSource.getMessage("customerResponseBuilder.cardMessage.cardsFound", new Object[]{cards.size()}, locale);
    }

    private String tinMessage(List<TinResponse> tins,CustomerEntity customer) {
        Locale locale = messageUtil.resolveLocale(customer);

        return tins.isEmpty()
                ? messageSource.getMessage("customerResponseBuilder.tinMessage.tinEmpty", null, locale)
                : messageSource.getMessage("customerResponseBuilder.tinMessage.tinFound", new Object[]{tins.size()}, locale);
    }

    public Page<TransactionResponse> buildCardTransactions(Integer customerId, String pan, int page) {
        Locale fallbackLocale = LocaleContextHolder.getLocale();
        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(pan)
                .orElseThrow(() -> new CardNotFoundException(messageSource.getMessage("customerResponseBuilder.buildCardTransactions.cardNotFound", null, fallbackLocale)));
        Locale locale = messageUtil.resolveLocale(card.getCustomer());
        if (!card.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException(messageSource.getMessage("customerResponseBuilder.buildCardTransactions.cardNotBelong", null, locale));
        }

        return transactionService.getTransactionsByCardId(card.getId(), page);
    }

    public Page<TransactionResponse> buildTinTransactions(Integer customerId, String tinNumber, int page) {
        Locale fallbackLocale = LocaleContextHolder.getLocale();
        TinEntity tin = tinRepository.findByTinNumberAndIsVisibleTrue(tinNumber)
                .orElseThrow(() -> new TinNotFoundException(messageSource.getMessage("customerResponseBuilder.buildTinTransactions.tinNotFound",null, fallbackLocale)));
        Locale locale = messageUtil.resolveLocale(tin.getCustomer());
        if (!tin.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException(messageSource.getMessage("customerResponseBuilder.buildTinTransactions.tinNotBelong",null, locale));
        }

        return transactionService.getTransactionsByTinId(tin.getId(), page);
    }
}
