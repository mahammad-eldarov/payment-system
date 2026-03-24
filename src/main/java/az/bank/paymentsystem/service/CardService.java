package az.bank.paymentsystem.service;

import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.util.shared.CardBalanceTransfer;
import az.bank.paymentsystem.util.shared.MessageUtil;
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
import az.bank.paymentsystem.util.card.CardValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final MessageUtil messageUtil;

    public MessageResponse deleteCard(Integer cardId) {
        CardEntity card = findActiveCard(cardId);
        Locale locale = messageUtil.resolveLocale(card.getCustomer());

        cardValidator.validateCardDeletion(card);
        statusAuditLogger.logCard(card, CardStatus.CLOSED.name(), messageSource.getMessage("cardService.deleteCard.cardClosed",null,locale));

        card.setStatus(CardStatus.CLOSED);
        card.setIsVisible(false);
        card.setUpdatedAt(Instant.now());

        cardBalanceTransfer.transfer(card,locale);
        cardRepository.save(card);
        return new MessageResponse(messageSource.getMessage("cardService.deleteCard.cardClosedSuccessfully",null,locale));
    }

    @Transactional
    public void updateExpiredCards() {

        List<CardEntity> expiredCards = cardRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CardStatus.EXPIRED);

        expiredCards.forEach(card -> {
            Locale locale = messageUtil.resolveLocale(card.getCustomer());

            statusAuditLogger.logCard(card, CardStatus.EXPIRED.name(), messageSource.getMessage("cardService.updateExpiredCards.cardExpiry",null,locale));
            card.setStatus(CardStatus.EXPIRED);
            card.setUpdatedAt(Instant.now());
            cardBalanceTransfer.transfer(card,locale);
        });

        cardRepository.saveAll(expiredCards);
    }

    public MessageResponse updateCardStatus(Integer id, CardStatus status) {
        CardEntity card = findActiveCard(id);
        Locale locale = LocaleContextHolder.getLocale();
        statusAuditLogger.logCard(card, status.name(), messageSource.getMessage("cardService.updateCardStatus.manualUpdate",null,locale));
        card.setStatus(status);
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
        return new MessageResponse(messageSource.getMessage("cardService.updateCardStatus.manualUpdateSuccess",null,locale));
    }

    public MessageResponse updateCardPassword(Integer id, UpdateCardPasswordRequest request) {
        CardEntity card = findActiveCard(id);
        Locale locale = LocaleContextHolder.getLocale();
        card.setPassword(request.getPassword());
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
        return new MessageResponse(messageSource.getMessage("cardService.updateCardPassword.updateResponse",null,locale));
    }

    public List<CardResponse> getCardsByCustomerId(Integer customerId) {
        findActiveCustomer(customerId);
        Locale locale = LocaleContextHolder.getLocale();
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

    //pageable
//    public List<CardResponse> getCardsByStatus(CardStatus status) {
//        List<CardEntity> cards = cardRepository.findByStatus(status);
//        Locale locale = LocaleContextHolder.getLocale();
//        if (cards.isEmpty()) {
//            throw new CardNotFoundException(messageSource.getMessage("cardService.getCardsByStatus.cardNotFound", null, locale));
//        }
//        return cards.stream().map(cardMapper::toResponse).collect(Collectors.toList());
//    }

    public Page<CardResponse> getCardsByStatus(CardStatus status, int page) {
        Locale locale = LocaleContextHolder.getLocale();

        if (page < 1) throw new PageRequestException(messageSource.getMessage("statusAuditLogService.buildPageable.pageNumber", null, locale));

        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());

        Page<CardEntity> cards = cardRepository.findByStatus(status, pageable);

        if (cards.isEmpty()) {
            throw new CardNotFoundException(messageSource.getMessage("cardService.getCardsByStatus.cardNotFound", null, locale));
        }

        return cards.map(cardMapper::toResponse);
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