package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.NotificationEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
    Page<NotificationEntity> findByCustomerIdOrderByCreatedAtDesc(Integer customerId, Pageable pageable);
    Integer countByCustomerIdAndIsReadFalse(Integer customerId);
    List<NotificationEntity> findByCustomerIdAndIsReadFalse(Integer customerId);
}
