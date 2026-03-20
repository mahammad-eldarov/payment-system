package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.request.CardToExternalRequest;
import az.bank.paymentsystem.dto.request.CurrentAccountToExternalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.AccountToAccountRequest;
import az.bank.paymentsystem.dto.request.AccountToCardRequest;
import az.bank.paymentsystem.dto.request.CardToAccountRequest;
import az.bank.paymentsystem.dto.request.CardToCardRequest;
import az.bank.paymentsystem.dto.response.PaymentResponse;
import az.bank.paymentsystem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Payment and Transaction Management APIs.")
public class PaymentV1Controller {

    private final PaymentService paymentService;

    // POST /api/payments/customer/{customerId}/card-to-card
    @PostMapping("/{customerId}/card-to-card")
    @Operation(summary = "Card → Card payment.", description = "Transfer from a card PAN to another card PAN.")
    public ResponseEntity<PaymentResponse> cardToCard(
            @PathVariable Integer customerId,
            @RequestBody CardToCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.cardToCard(customerId, request));
    }

    // POST /api/payments/customer/{customerId}/card-to-account
    @PostMapping("/{customerId}/card-to-account")
    @Operation(summary = "Card → Account payment.", description = "Transfer from a card PAN to a current account number.")
    public ResponseEntity<PaymentResponse> cardToAccount(
            @PathVariable Integer customerId,
            @RequestBody CardToAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.cardToAccount(customerId, request));
    }

    // POST /api/payments/customer/{customerId}/account-to-card
    @PostMapping("/{customerId}/account-to-card")
    @Operation(summary = "Account → Card payment.", description = "Transfer from a current account number to a card PAN.")
    public ResponseEntity<PaymentResponse> accountToCard(
            @PathVariable Integer customerId,
            @RequestBody AccountToCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.accountToCard(customerId, request));
    }

    // POST /api/payments/customer/{customerId}/account-to-account
    @PostMapping("/{customerId}/account-to-account")
    @Operation(summary = "Account → Account payment.", description = "Transfer from a current account number to another account number.")
    public ResponseEntity<PaymentResponse> accountToAccount(
            @PathVariable Integer customerId,
            @RequestBody AccountToAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.accountToAccount(customerId, request));
    }

    @PostMapping("/{customerId}/card-to-external")
    @Operation(summary = "Card → External Party payment.")
    public ResponseEntity<PaymentResponse> cardToExternal(
            @PathVariable Integer customerId,
            @RequestBody CardToExternalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.cardToExternal(customerId, request));
    }

    @PostMapping("/{customerId}/account-to-external")
    @Operation(summary = "Account → External Party payment.")
    public ResponseEntity<PaymentResponse> accountToExternal(
            @PathVariable Integer customerId,
            @RequestBody CurrentAccountToExternalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.accountToExternal(customerId, request));
    }


    // GET /api/payments/{paymentId}
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID.", description = "Returns payment details.")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Integer paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }


}
