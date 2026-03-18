package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.OrderRateLimitEntity;
import az.bank.paymentsystem.enums.OrderType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRateLimitRepository extends JpaRepository<OrderRateLimitEntity, Integer> {

    Optional<OrderRateLimitEntity> findByCustomerIdAndOrderType(
            Integer customerId, OrderType orderType
    );
}