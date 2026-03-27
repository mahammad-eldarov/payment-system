package az.bank.paymentsystem.repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<CardEntity, Integer> {

    List<CardEntity> findCardsByCustomerId(Integer customerId);

    @EntityGraph(attributePaths = {"customer"})
    Optional<CardEntity> findByIdAndIsVisibleTrue(Integer id);

    Integer countByCustomerIdAndIsVisibleTrue(Integer customerId);

    Boolean existsByCustomerIdAndStatusIn(Integer customerId, List<CardStatus> statuses);

    Boolean existsByPan(String pan);

    @EntityGraph(attributePaths = {"customer"})
    Optional<CardEntity> findByPanAndIsVisibleTrue(String pan);

    @EntityGraph(attributePaths = {"customer"})
    List<CardEntity> findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate date, CardStatus status);

    Page<CardEntity> findByStatus(CardStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT c FROM CardEntity c WHERE c.customer.id = :customerId AND c.isVisible = true AND c.balance >= :amount ORDER BY c.balance ASC LIMIT 1")
    Optional<CardEntity> findSufficientCard(@Param("customerId") Integer customerId, @Param("amount") BigDecimal amount);

    Optional<CardEntity> findFirstByCustomerIdAndIsVisibleTrueAndIdNot(Integer customerId, Integer cardId);

    Boolean existsByCustomerIdAndStatus(Integer customerId, CardStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CardEntity c WHERE c.id = :id")
    Optional<CardEntity> findByIdWithLock(Integer id);
}
