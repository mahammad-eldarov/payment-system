package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.TinEntity;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import az.bank.paymentsystem.enums.TinStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TinRepository extends JpaRepository<TinEntity, Integer> {

    List<TinEntity> findTinByCustomerId(Integer customerId);

    @EntityGraph(attributePaths = {"customer"})
    Optional<TinEntity> findByIdAndIsVisibleTrue(Integer id);

    Integer countByCustomerIdAndIsVisibleTrue(Integer customerId);

    @EntityGraph(attributePaths = {"customer"})
    Optional<TinEntity> findByTinNumberAndIsVisibleTrue(String tinNumber);

    Boolean existsByTinNumber(String tinNumber);

    @EntityGraph(attributePaths = {"customer"})
    List<TinEntity> findByCustomerIdAndIsVisibleTrue(Integer id);

    Page<TinEntity> findByStatus(TinStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"customer"})
    List<TinEntity> findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate date, TinStatus status);

    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT a FROM TinEntity a WHERE a.customer.id = :customerId AND a.isVisible = true AND a.balance >= :amount ORDER BY a.balance DESC LIMIT 1")
    Optional<TinEntity> findSufficientTin(@Param("customerId") Integer customerId, @Param("amount") BigDecimal amount);

    Boolean existsByCustomerIdAndStatus(Integer customerId, TinStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM TinEntity a WHERE a.id = :id")
    Optional<TinEntity> findByIdWithLock(Integer id);
}
