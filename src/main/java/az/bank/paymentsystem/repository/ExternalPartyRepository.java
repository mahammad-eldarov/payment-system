package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.ExternalPartyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalPartyRepository extends JpaRepository<ExternalPartyEntity, Integer> {

    Optional<ExternalPartyEntity> findByCardNumber(String cardNumber);

    Optional<ExternalPartyEntity> findByAccountNumber(String accountNumber);

}
