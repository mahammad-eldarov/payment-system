package az.bank.paymentsystem.repository;

import java.util.List;
import java.util.Optional;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {

    @EntityGraph(attributePaths = {"cardEntity", "currentAccountEntity", "transactionEntity"})
    Optional<CustomerEntity> findById(Integer id);

    Optional<CustomerEntity> findByIdAndIsVisibleTrue(Integer id);

    List<CustomerEntity> findAllByIsVisibleTrue();

    Optional<CustomerEntity> findByIdAndIsVisibleFalse(Integer id);

    Optional<CustomerEntity> findByEmailAndIsVisibleTrue(String email);

    Optional<CustomerEntity> findByPhoneNumberAndIsVisibleTrue(String phoneNumber);

    Boolean existsByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByPin(String pin);

    Boolean existsByPinAndIsVisibleTrue(String pin);

    List<CustomerEntity> findByStatusAndIsVisibleTrue(CustomerStatus status);

    List<CustomerEntity> findByStatusAndIsVisibleFalse(CustomerStatus status);



}
