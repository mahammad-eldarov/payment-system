package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.dto.response.NotificationResponse;
import az.bank.paymentsystem.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Customer notification management")
public class NotificationV1Controller {

    private final NotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get notifications", description = "Returns paginated notifications for a customer")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(notificationService.getNotifications(customerId, page).getContent());
    }

//    @GetMapping("/customer/{customerId}/unread-count")
//    @Operation(summary = "Get unread count", description = "Returns unread notification count for a customer")
//    public ResponseEntity<Long> getUnreadCount(@PathVariable Integer customerId) {
//        return ResponseEntity.ok(notificationService.getUnreadCount(customerId));
//    }
//
//    @PatchMapping("/{notificationId}/read")
//    @Operation(summary = "Mark as read", description = "Marks a notification as read")
//    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Integer notificationId) {
//        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
//    }
//
//    @PatchMapping("/customer/{customerId}/read-all")
//    @Operation(summary = "Mark all as read", description = "Marks all notifications as read for a customer")
//    public ResponseEntity<MessageResponse> markAllAsRead(@PathVariable Integer customerId) {
//        return ResponseEntity.ok(notificationService.markAllAsRead(customerId));
//    }
}


//@GetMapping("/card/{cardId}")
//@Operation(summary = "Get last 100 transactions by card ID.",
//        description = "Returns transactions where card is sender or receiver.")
//public ResponseEntity<List<TransactionResponse>> getByCardId(
//        @PathVariable Integer cardId,
//        @RequestParam(required = false, defaultValue = "0") int page) {
//    return ResponseEntity.ok(transactionService.getTransactionsByCardId(cardId, page).getContent());
//}
