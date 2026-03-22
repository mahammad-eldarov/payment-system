package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.dto.response.NotificationResponse;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.NotificationEntity;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.NotificationNotFoundException;
import az.bank.paymentsystem.mapper.NotificationMapper;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CustomerService customerService;

    public void send(CustomerEntity customer, String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setCustomer(customer);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    public Page<NotificationResponse> getNotifications(Integer customerId, int page) {
//        customerRepository.findByIdAndIsVisibleTrue(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        customerService.findActiveCustomer(customerId);
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
        Page<NotificationEntity> notifications = notificationRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        if (notifications.isEmpty()) throw new EmptyListException("notificationService.getNotifications.notificationNotFound");

        List<NotificationEntity> unread = notifications.getContent().stream()
                .filter(n -> !n.getIsRead()).toList();

        if (!unread.isEmpty()) {
            unread.forEach(n -> n.setIsRead(true));
            notificationRepository.saveAll(unread);
        }

        return notifications.map(notificationMapper::toResponse);
    }

//    public long getUnreadCount(Integer customerId) {
//        return notificationRepository.countByCustomerIdAndIsReadFalse(customerId);
//    }
//
//    public MessageResponse markAsRead(Integer notificationId) {
//        NotificationEntity notification = notificationRepository.findById(notificationId)
//                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
//        notification.setIsRead(true);
//        notificationRepository.save(notification);
//        return new MessageResponse("Notification marked as read.");
//    }
//
//    public MessageResponse markAllAsRead(Integer customerId) {
//        List<NotificationEntity> unread = notificationRepository.findByCustomerIdAndIsReadFalse(customerId);
//        unread.forEach(n -> n.setIsRead(true));
//        notificationRepository.saveAll(unread);
//        return new MessageResponse("All notifications marked as read.");
//    }
}
