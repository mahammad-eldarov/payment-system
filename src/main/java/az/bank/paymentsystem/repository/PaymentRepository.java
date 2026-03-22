package az.bank.paymentsystem.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "fromCard.customer",
            "toCard", "toCard.customer",
            "fromAccount", "fromAccount.customer",
            "toAccount", "toAccount.customer"
    })
    Optional<PaymentEntity> findById(Integer id);

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "fromCard.customer",
            "toCard", "toCard.customer",
            "fromAccount", "fromAccount.customer",
            "toAccount", "toAccount.customer"
    })
    List<PaymentEntity> findAllByStatus(PaymentStatus status);


    Boolean existsByCustomerIdAndScheduledDateAndStatus(
            Integer customerId,
            LocalDate scheduledDate,
            PaymentStatus status
    );

}