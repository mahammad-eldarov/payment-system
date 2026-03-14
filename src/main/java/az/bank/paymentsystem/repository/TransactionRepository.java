package az.bank.paymentsystem.repository;

import java.math.BigDecimal;
import java.time.Instant;
import az.bank.paymentsystem.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "toCard",
            "fromAccount", "toAccount",
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
            "fromAccount", "toAccount",
            "payment"
    })
    Page<TransactionEntity> findByFromAccountIdOrToAccountId(
            Integer fromAccountId,
            Integer toAccountId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "customer",
            "fromCard", "toCard",
            "fromAccount", "toAccount",
            "payment"
    })
    Page<TransactionEntity> findAllByPaymentId(Integer paymentId, Pageable pageable);

//    Page<TransactionEntity> findAllByPaymentId(Integer paymentId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t " +
            "WHERE t.customer.id = :customerId " +
            "AND t.status = az.bank.paymentsystem.enums.TransactionStatus.SUCCESS " +
            "AND t.createdAt >= :startOfMonth " +
            "AND t.createdAt <= :endOfMonth")

    BigDecimal sumByCustomerAndMonth(
            @Param("customerId") Integer customerId,
            @Param("startOfMonth") Instant startOfMonth,
            @Param("endOfMonth") Instant endOfMonth
    );

//    List<TransactionEntity> findAllByPaymentId(Integer paymentId);

//    List<TransactionEntity> findByFromCardId(Integer fromCardId);
//
//    List<TransactionEntity> findByFromAccountId(Integer fromAccountId);

}
//findByIdAndIsVisibleTrue
//(Integer customerId);