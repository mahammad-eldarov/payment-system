package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.enums.PaymentSourceType;
import java.time.LocalDate;
import java.util.Optional;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.PaymentStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {

    @NonNull
    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "fromCard.customer",
            "toCard", "toCard.customer",
            "fromAccount", "fromAccount.customer",
            "toAccount", "toAccount.customer"
    })
    Optional<PaymentEntity> findById(Integer id);

    Boolean existsByCustomerIdAndScheduledDateAndFromType(
            Integer customerId, LocalDate scheduledDate, PaymentSourceType fromType);

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "fromCard.customer",
            "toCard", "toCard.customer",
            "fromAccount", "fromAccount.customer",
            "toAccount", "toAccount.customer"
    })
    Optional<PaymentEntity> findByIdAndCustomerId(Integer id, Integer customerId);

    Boolean existsByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "fromCard.customer",
            "toCard", "toCard.customer",
            "fromAccount", "fromAccount.customer",
            "toAccount", "toAccount.customer"
    })
    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);

    Page<PaymentEntity> findAllByStatusOrderByCreatedAtAsc(PaymentStatus status, Pageable pageable);

}