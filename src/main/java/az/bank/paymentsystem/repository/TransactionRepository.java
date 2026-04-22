package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "toCard",
            "fromTin", "toTin",
            "payment"
    })
    Page<TransactionEntity> findByFromCardIdOrToCardId(
            Integer fromCardId,
            Integer toCardId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "toCard",
            "fromTin", "toTin",
            "payment"
    })
    Page<TransactionEntity> findByFromTinIdOrToTinId(
            Integer fromTinId,
            Integer toTinId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "toCard",
            "fromTin", "toTin",
            "payment"
    })
    Page<TransactionEntity> findAllByPaymentId(Integer paymentId, Pageable pageable);


}