package az.bank.paymentsystem.service;


import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.util.payment.PaymentCooldownChecker;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.TinToTinRequest;
import az.bank.paymentsystem.dto.request.TinToCardRequest;
import az.bank.paymentsystem.dto.request.CardToTinRequest;
import az.bank.paymentsystem.dto.request.CardToCardRequest;
import az.bank.paymentsystem.dto.response.PaymentResponse;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.PaymentNotFoundException;
import az.bank.paymentsystem.mapper.PaymentMapper;
import az.bank.paymentsystem.repository.PaymentRepository;
import az.bank.paymentsystem.util.payment.PaymentCreator;
import az.bank.paymentsystem.util.payment.PaymentProcessor;
import az.bank.paymentsystem.util.payment.PaymentSourceResolver;
import az.bank.paymentsystem.util.payment.PaymentValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;
    private final PaymentProcessor paymentProcessor;
    private final PaymentSourceResolver paymentSourceResolver;
    private final PaymentMapper paymentMapper;
    private final PaymentCreator paymentCreator;
    private final PaymentCooldownChecker paymentCooldownChecker;
    private final MessageSource messageSource;
    private final CustomerService customerService;
    private final MessageUtil messageUtil;


    @Transactional
    public PaymentResponse cardToCard(Integer customerId, CardToCardRequest request) {
        checkCooldown(customerId, request.getAmount(), request.getFromPan(), request.getToPan());

        String idempotencyKey = generateIdempotencyKey(
                customerId, request.getAmount(),
                request.getFromPan(), request.getToPan());

        Optional<PaymentResponse> idempotentResponse = checkIdempotency(idempotencyKey);
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.CARD, PaymentSourceType.CARD, idempotencyKey);

        paymentSourceResolver.fromCheckCard(payment, customerId, request.getFromPan(), errors);
        paymentSourceResolver.toCheckCard(payment, request.getToPan(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse cardToTin(Integer customerId, CardToTinRequest request) {
        checkCooldown(customerId, request.getAmount(), request.getFromPan(), request.getToTinNumber());

        String idempotencyKey = generateIdempotencyKey(
                customerId, request.getAmount(),
                request.getFromPan(), request.getToTinNumber());

        Optional<PaymentResponse> idempotentResponse = checkIdempotency(idempotencyKey);
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.CARD, PaymentSourceType.TIN, idempotencyKey);

        paymentSourceResolver.fromCheckCard(payment, customerId, request.getFromPan(), errors);
        paymentSourceResolver.toCheckTin(payment, request.getToTinNumber(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse tinToCard(Integer customerId, TinToCardRequest request) {
        checkCooldown(customerId, request.getAmount(), request.getFromTinNumber(), request.getToPan());

        String idempotencyKey = generateIdempotencyKey(
                customerId, request.getAmount(),
                request.getFromTinNumber(), request.getToPan());
        Optional<PaymentResponse> idempotentResponse = checkIdempotency(idempotencyKey);
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.TIN, PaymentSourceType.CARD, idempotencyKey);

        paymentSourceResolver.fromCheckTin(payment, customerId, request.getFromTinNumber(), errors);
        paymentSourceResolver.toCheckCard(payment, request.getToPan(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse tinToTin(Integer customerId, TinToTinRequest request) {
        checkCooldown(customerId, request.getAmount(), request.getFromTinNumber(), request.getToTinNumber());

        String idempotencyKey = generateIdempotencyKey(
                customerId, request.getAmount(),
                request.getFromTinNumber(), request.getToTinNumber());
        Optional<PaymentResponse> idempotentResponse = checkIdempotency(idempotencyKey);
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.TIN, PaymentSourceType.TIN, idempotencyKey);

        paymentSourceResolver.fromCheckTin(payment, customerId, request.getFromTinNumber(), errors);
        paymentSourceResolver.toCheckTin(payment, request.getToTinNumber(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    public void processPayments() {
        int page = 0;
        int pageSize = 100;
        Page<PaymentEntity> pageResult;

        do {
            pageResult = paymentRepository.findAllByStatusOrderByCreatedAtAsc(
                    PaymentStatus.PENDING,
                    PageRequest.of(page, pageSize));

            for (PaymentEntity payment : pageResult.getContent()) {
                paymentProcessor.process(payment.getId());
            }
            page++;
        } while (pageResult.hasNext());
    }

    public PaymentResponse getPaymentById(Integer customerId, Integer paymentId) {
        CustomerEntity customer = customerService.findActiveCustomer(customerId);
        Locale locale = messageUtil.resolveLocale(customer);
        PaymentEntity payment = paymentRepository.findByIdAndCustomerId(paymentId, customerId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        messageSource.getMessage("paymentService.findPaymentById.paymentNotFound", null, locale)
                ));
        return paymentMapper.toResponse(payment);
    }

    private Optional<PaymentResponse> checkIdempotency(String idempotencyKey) {
        if (idempotencyKey != null &&
                paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            return Optional.of(paymentMapper.toResponse(
                    paymentRepository.findByIdempotencyKey(idempotencyKey)
                            .orElseThrow(() -> new PaymentNotFoundException(
                                    messageSource.getMessage("paymentService.findPaymentById.paymentNotFound",
                                            null, LocaleContextHolder.getLocale())))));
        }
        return Optional.empty();
    }

    private String generateIdempotencyKey(Integer customerId, BigDecimal amount,
                                          String fromSource, String toSource) {
        String raw = customerId + ":" + amount + ":" + fromSource + ":" + toSource + ":" + Instant.now();
        return UUID.nameUUIDFromBytes(raw.getBytes()).toString();
    }

    private void checkCooldown(Integer customerId, BigDecimal amount, String fromSource, String toSource) {
        if (paymentCooldownChecker.isInCooldown(customerId, amount, fromSource, toSource)) {
            throw new MultiValidationException(List.of(
                    new ExceptionResponse(429,
                            messageSource.getMessage("paymentService.cooldown", null, LocaleContextHolder.getLocale()),
                            LocalDateTime.now())));
        }
    }

}