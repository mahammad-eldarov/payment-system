package az.bank.paymentsystem.service;

import az.bank.paymentsystem.exception.OperationNotAllowedException;
import az.bank.paymentsystem.util.shared.CardBalanceTransfer;
import az.bank.paymentsystem.util.shared.StatusAuditLogger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.UpdateCardPasswordRequest;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.mapper.CardMapper;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.card.CardCreator;
import az.bank.paymentsystem.util.card.CardValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final CardValidator cardValidator;
    private final CardMapper cardMapper;
    private final CardBalanceTransfer cardBalanceTransfer;
    private final StatusAuditLogger statusAuditLogger;
    private final MessageSource messageSource;

    public MessageResponse deleteCard(Integer cardId) {
        Locale locale =  LocaleContextHolder.getLocale();
        CardEntity card = findActiveCard(cardId);
        cardValidator.validateCardDeletion(card);
        statusAuditLogger.logCard(card, CardStatus.CLOSED.name(), messageSource.getMessage("cardService.deleteCard.cardClosed",null,locale));

        card.setStatus(CardStatus.CLOSED);
        card.setIsVisible(false);
        card.setUpdatedAt(Instant.now());

        cardBalanceTransfer.transfer(card);
        cardRepository.save(card);
        return new MessageResponse("cardService.deleteCard.cardClosedSuccessfully");
    }

    @Transactional
    public void updateExpiredCards() {
        Locale locale = LocaleContextHolder.getLocale();
        List<CardEntity> expiredCards = cardRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CardStatus.EXPIRED);

        expiredCards.forEach(card -> {
            statusAuditLogger.logCard(card, CardStatus.EXPIRED.name(), messageSource.getMessage("cardService.updateExpiredCards.cardExpiry",null,locale));
            card.setStatus(CardStatus.EXPIRED);
            card.setUpdatedAt(Instant.now());
            cardBalanceTransfer.transfer(card);
        });

        cardRepository.saveAll(expiredCards);
    }

    public MessageResponse updateCardStatus(Integer id, CardStatus status) {
        Locale locale = LocaleContextHolder.getLocale();
        CardEntity card = findActiveCard(id);
        statusAuditLogger.logCard(card, status.name(), messageSource.getMessage("cardService.updateCardStatus.manualUpdate",null,locale));
        card.setStatus(status);
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
        return new MessageResponse(messageSource.getMessage("cardService.updateCardStatus.manualUpdateSuccess",null,locale));
    }

    public MessageResponse updateCardPassword(Integer id, UpdateCardPasswordRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        CardEntity card = findActiveCard(id);
        card.setPassword(request.getPassword());
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
        return new MessageResponse(messageSource.getMessage("cardService.updateCardPassword.updateResponse",null,locale));
    }

    public List<CardResponse> getCardsByCustomerId(Integer customerId) {
        Locale locale = LocaleContextHolder.getLocale();
        findActiveCustomer(customerId);
        List<CardEntity> cards = cardRepository.findCardsByCustomerId(customerId);
        if (cards.isEmpty()) {
            throw new EmptyListException(messageSource.getMessage("cardService.getCardsByCustomerId.emptyListException",null,locale));
        }
        return cards.stream().map(cardMapper::toResponse).collect(Collectors.toList());
    }

    public CardResponse getCardByPan(String pan) {
        Locale locale = LocaleContextHolder.getLocale();
        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(pan).orElseThrow(() -> new CardNotFoundException(messageSource.getMessage("cardService.getCardByPan.cardNotFound",null,locale)));
        return cardMapper.toResponse(card);
    }

    public List<CardResponse> getCardsByStatus(CardStatus status) {
        Locale locale = LocaleContextHolder.getLocale();
        List<CardEntity> cards = cardRepository.findByStatusAndIsVisibleTrue(status);
        if (cards.isEmpty()) {
            throw new CardNotFoundException(messageSource.getMessage("cardService.getCardsByStatus.cardNotFound", null, locale));
        }
        return cards.stream().map(cardMapper::toResponse).collect(Collectors.toList());
    }

    public CardEntity findActiveCard(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();
        return cardRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CardNotFoundException(messageSource.getMessage("cardService.findActiveCard.cardNotFound", null, locale)));
    }

    public CustomerEntity findActiveCustomer(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();
        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("cardService.findActiveCustomer.customerNotFound", null, locale)));
    }


}