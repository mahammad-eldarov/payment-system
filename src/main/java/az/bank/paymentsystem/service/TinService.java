package az.bank.paymentsystem.service;

import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.util.shared.TinBalanceTransfer;
import az.bank.paymentsystem.util.shared.MessageUtil;
import az.bank.paymentsystem.util.shared.StatusAuditLogger;
import az.bank.paymentsystem.util.tin.TinValidator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.exception.TinNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.dto.response.TinResponse;
import az.bank.paymentsystem.mapper.TinMapper;
import az.bank.paymentsystem.repository.TinRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TinService {

    private final TinRepository tinRepository;
    private final CustomerRepository customerRepository;
    private final TinMapper tinMapper;
    private final TinValidator tinValidator;
    private final TinBalanceTransfer tinBalanceTransfer;
    private final StatusAuditLogger statusAuditLogger;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public List<TinResponse> getTinByCustomerId(Integer id) {
        findActiveCustomer(id);
        Locale locale = LocaleContextHolder.getLocale();

        List<TinEntity> tin = tinRepository.findByCustomerIdAndIsVisibleTrue(id);
        if (tin.isEmpty()) {
            throw new EmptyListException(messageSource.getMessage("tinService.getTinByCustomerId.customerNotHave", null, locale));
        }

        return tin.stream().map(tinMapper::toResponse).collect(Collectors.toList());
    }

    public TinResponse getTinByTinNumber(String tinNumber) {

        return tinMapper.toResponse(findActiveTinByNumber(tinNumber));
    }


    public Page<TinResponse> getTinByStatus(TinStatus status, int page) {
        Locale locale = LocaleContextHolder.getLocale();

        if (page < 1) throw new PageRequestException(messageSource.getMessage("statusAuditLogService.buildPageable.pageNumber", null, locale));

        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());

        Page<TinEntity> tin = tinRepository.findByStatus(status, pageable);

        if (tin.isEmpty()) {
            throw new TinNotFoundException(messageSource.getMessage("tinService.getTinByStatus.tinStatus", null, locale));
        }

        return tin.map(tinMapper::toResponse);
    }

    public MessageResponse updateTinStatus(Integer id, TinStatus status) {
        Locale locale = LocaleContextHolder.getLocale();

        TinEntity tin = findActiveTin(id);
        statusAuditLogger.logTin(tin, status.name(), messageSource.getMessage("tinService.updateTinStatus.manualUpdateStatus", null, locale));
        tin.setStatus(status);
        tin.setUpdatedAt(Instant.now());
        tinRepository.save(tin);
        return new MessageResponse(messageSource.getMessage("tinService.updateTinStatus.updateResponse",null, locale));
    }


    public void updateExpiredTin() {
        List<TinEntity> expiredTin = tinRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), TinStatus.EXPIRED);

        expiredTin.forEach(tin -> {
            Locale locale = messageUtil.resolveLocale(tin.getCustomer());
            statusAuditLogger.logTin(tin, TinStatus.EXPIRED.name(), messageSource.getMessage("tinService.updateExpiredTin.tinExpiry", null, locale));
            tin.setStatus(TinStatus.EXPIRED);
            tin.setUpdatedAt(Instant.now());
            tinBalanceTransfer.transfer(tin,locale);

        });

        tinRepository.saveAll(expiredTin);
    }

    public MessageResponse deleteTin(Integer id) {
        TinEntity tin = findActiveTin(id);
        Locale locale = messageUtil.resolveLocale(tin.getCustomer());
        tinValidator.validateDeletion(tin);
        statusAuditLogger.logTin(tin, TinStatus.CLOSED.name(), messageSource.getMessage("tinService.deleteTin.tinClosed", null, locale));
        tin.setStatus(TinStatus.CLOSED);
        tin.setIsVisible(false);
        tin.setUpdatedAt(Instant.now());
        tinRepository.save(tin);
        return new MessageResponse(messageSource.getMessage("tinService.deleteTin.tinClosedSuccessfully", null, locale));
    }


    public TinEntity findActiveTin(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        return tinRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new TinNotFoundException(messageSource.getMessage("tinService.findActiveTin.tinNotFound", null, locale)));
    }

    public TinEntity findActiveTinByNumber(String tinNumber) {
        Locale locale = LocaleContextHolder.getLocale();

        return tinRepository.findByTinNumberAndIsVisibleTrue(tinNumber)
                .orElseThrow(() -> new TinNotFoundException(messageSource.getMessage("tinService.findActiveTinByNumber.tinNotFound", null, locale)));
    }

    public CustomerEntity findActiveCustomer(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("tinService.findActiveCustomer.customerNotFound",null, locale)));
    }

}