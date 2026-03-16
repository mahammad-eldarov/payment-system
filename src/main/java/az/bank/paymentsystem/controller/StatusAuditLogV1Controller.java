package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.response.StatusAuditLogResponse;
import az.bank.paymentsystem.service.StatusAuditLogService;
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
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "Status change history for cards, accounts and customers")
public class StatusAuditLogV1Controller {

    private final StatusAuditLogService statusAuditLogService;

    @GetMapping("/card/{cardId}")
    @Operation(summary = "Get card status history", description = "Returns status change history for a specific card")
    public ResponseEntity<List<StatusAuditLogResponse>> getCardHistory(@PathVariable Integer cardId,
                                                                       @RequestParam(required = false, defaultValue ="1")
                                                                       int page) {
        return ResponseEntity.ok(statusAuditLogService.getCardHistory(cardId,page).getContent());
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get account status history", description = "Returns status change history for a specific current account")
    public ResponseEntity<List<StatusAuditLogResponse>> getAccountHistory(@PathVariable Integer accountId,
                                                                          @RequestParam(required = false, defaultValue ="1")
                                                                          int page) {
        return ResponseEntity.ok(statusAuditLogService.getAccountHistory(accountId, page).getContent());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer status history", description = "Returns status change history for a specific customer")
    public ResponseEntity<List<StatusAuditLogResponse>> getCustomerHistory(@PathVariable Integer customerId,
                                                                           @RequestParam(required = false, defaultValue ="1")
                                                                           int page) {
        return ResponseEntity.ok(statusAuditLogService.getCustomerHistory(customerId,page).getContent());
    }
}