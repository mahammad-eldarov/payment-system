package az.bank.paymentsystem.service;


import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.AccountToAccountRequest;
import az.bank.paymentsystem.dto.request.AccountToCardRequest;
import az.bank.paymentsystem.dto.request.CardToAccountRequest;
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
    private final MessageSource messageSource;
    private final CustomerService customerService;
    private final MessageUtil messageUtil;


    @Transactional
    public PaymentResponse cardToCard(Integer customerId, CardToCardRequest request) {

        Optional<PaymentResponse> idempotentResponse = checkIdempotency(request.getIdempotencyKey());
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.CARD, PaymentSourceType.CARD, request.getIdempotencyKey());

        paymentSourceResolver.fromCheckCard(payment, customerId, request.getFromPan(), errors);
        paymentSourceResolver.toCheckCard(payment, request.getToPan(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse cardToAccount(Integer customerId, CardToAccountRequest request) {
        Optional<PaymentResponse> idempotentResponse = checkIdempotency(request.getIdempotencyKey());
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.CARD, PaymentSourceType.CURRENT_ACCOUNT, request.getIdempotencyKey());

        paymentSourceResolver.fromCheckCard(payment, customerId, request.getFromPan(), errors);
        paymentSourceResolver.toCheckAccount(payment, request.getToAccountNumber(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse accountToCard(Integer customerId, AccountToCardRequest request) {
        Optional<PaymentResponse> idempotentResponse = checkIdempotency(request.getIdempotencyKey());
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.CURRENT_ACCOUNT, PaymentSourceType.CARD, request.getIdempotencyKey());

        paymentSourceResolver.fromCheckAccount(payment, customerId, request.getFromAccountNumber(), errors);
        paymentSourceResolver.toCheckCard(payment, request.getToPan(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse accountToAccount(Integer customerId, AccountToAccountRequest request) {
        Optional<PaymentResponse> idempotentResponse = checkIdempotency(request.getIdempotencyKey());
        if (idempotentResponse.isPresent()) return idempotentResponse.get();

        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validateAmount(request.getAmount(), errors);

        PaymentEntity payment = paymentCreator.buildPayment(customerId, request.getAmount(),
                PaymentSourceType.CURRENT_ACCOUNT, PaymentSourceType.CURRENT_ACCOUNT, request.getIdempotencyKey());

        paymentSourceResolver.fromCheckAccount(payment, customerId, request.getFromAccountNumber(), errors);
        paymentSourceResolver.toCheckAccount(payment, request.getToAccountNumber(), errors);
        paymentValidator.checkSelfTransfer(payment, errors);

        if (!errors.isEmpty()) throw new MultiValidationException(errors);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }


    @Transactional
    public void processPayments() {
        List<PaymentEntity> pendingPayments = paymentRepository.findAllByStatus(PaymentStatus.PENDING);
        for (PaymentEntity payment : pendingPayments) {
            paymentProcessor.process(payment.getId());
        }
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
                                    messageSource.getMessage("paymentService.findPaymentById.paymentNotFound", null, LocaleContextHolder.getLocale())))));
        }
        return Optional.empty();
    }

}