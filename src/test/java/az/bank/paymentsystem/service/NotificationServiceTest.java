package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.NotificationResponse;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.NotificationEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.Language;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.mapper.NotificationMapper;
import az.bank.paymentsystem.repository.NotificationRepository;
import az.bank.paymentsystem.util.shared.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;
    @Mock private CustomerService customerService;
    @Mock private MessageSource messageSource;
    @Mock private MessageUtil messageUtil;

    @Captor private ArgumentCaptor<NotificationEntity> notificationCaptor;
    @Captor private ArgumentCaptor<List<NotificationEntity>> notificationListCaptor;

    @InjectMocks
    private NotificationService notificationService;

    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        customer = new CustomerEntity();
        customer.setId(1);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setLanguage(Language.EN);
        customer.setIsVisible(true);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("mocked-message");
        lenient().when(messageUtil.resolveLocale(any(CustomerEntity.class)))
                .thenReturn(Language.EN.toLocale());
    }

    @Test
    void shouldSaveNotificationWithCorrectFieldsWhenSendIsCalled() {
        notificationService.send(customer, "Test message");

        verify(notificationRepository).save(notificationCaptor.capture());
        NotificationEntity saved = notificationCaptor.getValue();

        assertEquals(customer, saved.getCustomer());
        assertEquals("Test message", saved.getMessage());
        assertFalse(saved.getIsRead());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void shouldReturnMappedPageAndMarkUnreadNotificationsAsReadWhenNotificationsExist() {
        NotificationEntity unreadNotification = new NotificationEntity();
        unreadNotification.setId(1);
        unreadNotification.setIsRead(false);

        NotificationResponse expected = new NotificationResponse();
        expected.setId(1);

        Page<NotificationEntity> entityPage = new PageImpl<>(List.of(unreadNotification));

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(eq(1), any(Pageable.class)))
                .thenReturn(entityPage);
        when(notificationMapper.toResponse(unreadNotification)).thenReturn(expected);

        Page<NotificationResponse> actual = notificationService.getNotifications(1, 1);

        verify(notificationRepository).saveAll(notificationListCaptor.capture());
        List<NotificationEntity> savedUnread = notificationListCaptor.getValue();

        assertEquals(1, actual.getContent().size());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
        assertTrue(savedUnread.get(0).getIsRead());
    }

    @Test
    void shouldNotCallSaveAllWhenAllNotificationsAreAlreadyRead() {
        NotificationEntity readNotification = new NotificationEntity();
        readNotification.setId(1);
        readNotification.setIsRead(true);

        NotificationResponse expected = new NotificationResponse();
        expected.setId(1);

        Page<NotificationEntity> entityPage = new PageImpl<>(List.of(readNotification));

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(eq(1), any(Pageable.class)))
                .thenReturn(entityPage);
        when(notificationMapper.toResponse(readNotification)).thenReturn(expected);

        Page<NotificationResponse> actual = notificationService.getNotifications(1, 1);

        verify(notificationRepository, never()).saveAll(any());
        assertEquals(expected.getId(), actual.getContent().get(0).getId());
    }

    @Test
    void shouldThrowEmptyListExceptionWhenCustomerHasNoNotifications() {
        Class<EmptyListException> expected = EmptyListException.class;

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(eq(1), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(expected, () -> notificationService.getNotifications(1, 1));
    }

    @Test
    void shouldMarkOnlyUnreadNotificationsAsReadWhenMixedNotificationsExist() {
        NotificationEntity unreadNotification = new NotificationEntity();
        unreadNotification.setId(1);
        unreadNotification.setIsRead(false);

        NotificationEntity readNotification = new NotificationEntity();
        readNotification.setId(2);
        readNotification.setIsRead(true);

        Page<NotificationEntity> entityPage = new PageImpl<>(List.of(unreadNotification, readNotification));

        when(customerService.findActiveCustomer(1)).thenReturn(customer);
        when(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(eq(1), any(Pageable.class)))
                .thenReturn(entityPage);
        when(notificationMapper.toResponse(any())).thenReturn(new NotificationResponse());

        notificationService.getNotifications(1, 1);

        verify(notificationRepository).saveAll(notificationListCaptor.capture());
        List<NotificationEntity> savedUnread = notificationListCaptor.getValue();

        assertEquals(1, savedUnread.size());
        assertEquals(1, savedUnread.get(0).getId());
        assertTrue(savedUnread.get(0).getIsRead());
    }
}