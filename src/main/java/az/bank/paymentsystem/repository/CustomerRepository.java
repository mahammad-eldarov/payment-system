package az.bank.paymentsystem.repository;

import java.util.List;
import java.util.Optional;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {

    @EntityGraph(attributePaths = {"cardEntity", "currentAccountEntity", "transactionEntity"})
    Optional<CustomerEntity> findById(Integer id);

    Optional<CustomerEntity> findByIdAndIsVisibleTrue(Integer id);

    Page<CustomerEntity> findAllByIsVisibleTrue(Pageable pageable);

    Optional<CustomerEntity> findByIdAndIsVisibleFalse(Integer id);

    Boolean existsByPinAndIsVisibleTrue(String pin);

    Page<CustomerEntity> findByStatus(CustomerStatus status, Pageable pageable);

    List<CustomerEntity> findByStatusAndIsVisibleFalse(CustomerStatus status);

    Optional<CustomerEntity> findFirstByPinAndIsVisibleFalse(String pin);

    List<CustomerEntity> findAllByPinAndIsVisibleFalse(String pin);



}
