package az.bank.paymentsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.service.CurrentAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/current-accounts")
@RequiredArgsConstructor
@Tag(name = "Current Account Controller", description = "Current Account Management APIs.")
public class CurrentAccountV1Controller {

    private final CurrentAccountService currentAccountService;

    // POST /api/current-accounts/order/{customerId}
//    @PostMapping("/order/{customerId}")
//    @Operation(summary = "Create a current account.", description = "Creates a new current account for a customer.")
//    public ResponseEntity<CurrentAccountResponse> orderCurrentAccount(
//            @PathVariable Integer customerId,
//            @Valid @RequestBody OrderCurrentAccountRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(currentAccountService.orderCurrentAccount(customerId, request));
//    }

    @PatchMapping("/{currentAccountId}/status")
    @Operation(summary = "Update current account status using ID.",
            description = "Update current account status.")
    public ResponseEntity<MessageResponse> updateCurrentAccountStatus(
            @PathVariable Integer currentAccountId,
            @RequestParam CurrentAccountStatus status) {

        return ResponseEntity.ok(currentAccountService.updateCurrentAccountStatus(currentAccountId, status));
    }

    // GET /api/current-accounts/customer/{customerId}
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get accounts by customer ID.", description = "Retrieves all current accounts for a customer.")
    public ResponseEntity<List<CurrentAccountResponse>> getCurrentAccountsByCustomerId(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(currentAccountService.getAccountsByCustomerId(customerId));
    }

    // GET /api/current-accounts/{accountNumber}
    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by account number.", description = "Retrieves a current account by its account number.")
    public ResponseEntity<CurrentAccountResponse> getCurrentAccountByAccountNumber(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(currentAccountService.getAccountByAccountNumber(accountNumber));
    }

    @GetMapping("/status")
    @Operation(summary = "Get a current account status.", description = "Get current account by status.")
    public ResponseEntity<List<CurrentAccountResponse>> getCurrentAccountByStatus(
            @RequestParam CurrentAccountStatus status) {

        return ResponseEntity.ok(currentAccountService.getCurrentAccountByStatus(status));
    }

    // DELETE /api/current-accounts/{id}/delete
    @DeleteMapping("/{currentAccountId}/delete")
    @Operation(summary = "Delete a current account.", description = "Soft-deletes a current account using its ID.")
    public ResponseEntity<MessageResponse> deleteCurrentAccount(@PathVariable Integer currentAccountId) {

        return ResponseEntity.ok(currentAccountService.deleteCurrentAccount(currentAccountId));
    }
}
