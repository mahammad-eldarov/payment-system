package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.NotificationResponse;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.NotificationEntity;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.mapper.NotificationMapper;
import az.bank.paymentsystem.repository.NotificationRepository;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
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
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public void send(CustomerEntity customer, String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setCustomer(customer);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    public Page<NotificationResponse> getNotifications(Integer customerId, int page) {
        CustomerEntity customer = customerService.findActiveCustomer(customerId);

        Locale locale = messageUtil.resolveLocale(customer);
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
        Page<NotificationEntity> notifications = notificationRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        if (notifications.isEmpty()) throw new EmptyListException(messageSource.getMessage("notificationService.getNotifications.notificationNotFound", null, locale));

        List<NotificationEntity> unread = notifications.getContent().stream()
                .filter(n -> !n.getIsRead()).toList();

        if (!unread.isEmpty()) {
            unread.forEach(n -> n.setIsRead(true));
            notificationRepository.saveAll(unread);
        }

        return notifications.map(notificationMapper::toResponse);
    }

}
