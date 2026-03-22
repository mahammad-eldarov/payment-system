package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.response.NotificationResponse;
import az.bank.paymentsystem.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

}

