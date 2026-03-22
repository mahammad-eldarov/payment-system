package az.bank.paymentsystem.service;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.OrderRateLimitEntity;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.CardOrderCooldownException;
import az.bank.paymentsystem.repository.OrderRateLimitRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderRateLimitService {

    private final OrderRateLimitRepository orderRateLimitRepository;

    public void checkCooldown(CustomerEntity customer, OrderType orderType) {
        orderRateLimitRepository.findByCustomerIdAndOrderType(customer.getId(), orderType)
                .ifPresent(limit -> {
                    if (limit.getCooldownUntil() != null &&
                            Instant.now().isBefore(limit.getCooldownUntil())) {
                        long minutesLeft = Duration.between(
                                Instant.now(), limit.getCooldownUntil()
                        ).toMinutes();
                        throw new CardOrderCooldownException(
                                "Too many rejected attempts. Please try again in " + minutesLeft + " minutes."
                        );
                    }
                    if (limit.getCooldownUntil() != null &&
                            Instant.now().isAfter(limit.getCooldownUntil())) {
                        resetLimit(customer, orderType);
                    }
                });
    }

    public void handleRejection(CustomerEntity customer, OrderType orderType) {
        OrderRateLimitEntity limit = orderRateLimitRepository
                .findByCustomerIdAndOrderType(customer.getId(), orderType)
                .orElse(new OrderRateLimitEntity(customer, orderType));

        int newCount = limit.getRejectionCount() + 1;
        limit.setRejectionCount(newCount);
        limit.setUpdatedAt(Instant.now());

        if (newCount >= 3) {
            limit.setCooldownUntil(Instant.now().plus(Duration.ofHours(1)));
        }

        orderRateLimitRepository.save(limit);
    }

    public void resetLimit(CustomerEntity customer, OrderType orderType) {
        orderRateLimitRepository.findByCustomerIdAndOrderType(customer.getId(), orderType)
                .ifPresent(limit -> {
                    limit.setRejectionCount(0);
                    limit.setCooldownUntil(null);
                    limit.setUpdatedAt(Instant.now());
                    orderRateLimitRepository.save(limit);
                });
    }
}
