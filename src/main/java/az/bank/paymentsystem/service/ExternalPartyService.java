package az.bank.paymentsystem.service;

import az.bank.paymentsystem.entity.ExternalPartyEntity;
import az.bank.paymentsystem.repository.ExternalPartyRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalPartyService {

    private final ExternalPartyRepository externalPartyRepository;

    public Optional<ExternalPartyEntity> findByCardNumber(String cardNumber) {
        return externalPartyRepository.findByCardNumber(cardNumber);
    }

    public Optional<ExternalPartyEntity> findByAccountNumber(String accountNumber) {
        return externalPartyRepository.findByAccountNumber(accountNumber);
    }
}
