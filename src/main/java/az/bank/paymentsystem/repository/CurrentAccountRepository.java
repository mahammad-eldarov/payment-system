package az.bank.paymentsystem.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurrentAccountRepository extends JpaRepository<CurrentAccountEntity, Integer> {

//    @Query("SELECT c FROM CurrentAccountEntity c WHERE c.customer.id = :customerId")
//    List<CurrentAccountEntity> findByCustomerIdForCurrentAccount(@Param("customerId") Integer customerId);

    List<CurrentAccountEntity> findCurrentAccountByCustomerId(Integer customerId);
    List<CurrentAccountEntity> findCurrentAccountByCustomerIdAndIsVisibleTrue(Integer customerId);

    @EntityGraph(attributePaths = {"customer"})
    Optional<CurrentAccountEntity> findByIdAndIsVisibleTrue(Integer id);

    @EntityGraph(attributePaths = {"customer"})
    List<CurrentAccountEntity> findAllByCustomerIdAndIsVisibleTrue(Integer customerId);

    Integer countByCustomerIdAndIsVisibleTrue(Integer customerId);

    @EntityGraph(attributePaths = {"customer"})
    Optional<CurrentAccountEntity> findByAccountNumberAndIsVisibleTrue(String accountNumber);

    @EntityGraph(attributePaths = {"customer"})
    List<CurrentAccountEntity> findAllByExpiryDateBeforeAndStatusNot(LocalDate date, CurrentAccountStatus status);

    Boolean existsByAccountNumber(String accountNumber);


    @EntityGraph(attributePaths = {"customer"})
    List<CurrentAccountEntity> findByCustomerIdAndIsVisibleTrue(Integer id);

    Optional<CurrentAccountEntity> findFirstByCustomerIdAndIsVisibleTrue(Integer customerId);

    List<CurrentAccountEntity> findByStatusAndIsVisibleTrue(CurrentAccountStatus status);

    List<CurrentAccountEntity> findByStatusAndIsVisibleFalse(CurrentAccountStatus status);

    @EntityGraph(attributePaths = {"customer"})
    List<CurrentAccountEntity> findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate date, CurrentAccountStatus status);

//    @Query("SELECT a FROM CurrentAccountEntity a WHERE a.customer.id = :customerId AND a.isVisible = true AND a.balance >= :amount ORDER BY a.balance ASC")
//    Optional<CurrentAccountEntity> findSufficientAccount(@Param("customerId") Integer customerId, @Param("amount") BigDecimal amount);

    @Query("SELECT a FROM CurrentAccountEntity a WHERE a.customer.id = :customerId AND a.isVisible = true AND a.balance >= :amount ORDER BY a.balance DESC LIMIT 1")
    Optional<CurrentAccountEntity> findSufficientAccount(@Param("customerId") Integer customerId, @Param("amount") BigDecimal amount);

}
