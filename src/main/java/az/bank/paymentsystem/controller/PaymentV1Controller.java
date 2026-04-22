package az.bank.paymentsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.TinToTinRequest;
import az.bank.paymentsystem.dto.request.TinToCardRequest;
import az.bank.paymentsystem.dto.request.CardToTinRequest;
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

    @PostMapping("/external/{customerId}/card-to-card")
    @Operation(summary = "Card → Card payment.", description = "Transfer from a card PAN to another card PAN.")
    public ResponseEntity<PaymentResponse> cardToCard(
            @PathVariable Integer customerId,
            @RequestBody CardToCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.cardToCard(customerId, request));
    }

    @PostMapping("/external/{customerId}/card-to-tin")
    @Operation(summary = "Card → TIN payment.", description = "Transfer from a card PAN to a TIN number.")
    public ResponseEntity<PaymentResponse> cardToTin(
            @PathVariable Integer customerId,
            @RequestBody CardToTinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.cardToTin(customerId, request));
    }

    @PostMapping("/external/{customerId}/tin-to-card")
    @Operation(summary = "TIN → Card payment.", description = "Transfer from a TIN number to a card PAN.")
    public ResponseEntity<PaymentResponse> tinToCard(
            @PathVariable Integer customerId,
            @RequestBody TinToCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.tinToCard(customerId, request));
    }

    @PostMapping("/external/{customerId}/tin-to-tin")
    @Operation(summary = "TIN → TIN payment.", description = "Transfer from a TIN number to another TIN number.")
    public ResponseEntity<PaymentResponse> tinToTin(
            @PathVariable Integer customerId,
            @RequestBody TinToTinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.tinToTin(customerId, request));
    }

    @GetMapping("/external/{customerId}/{paymentId}")
    @Operation(summary = "Get payment by ID.", description = "Returns payment details.")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Integer customerId, @PathVariable Integer paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(customerId,paymentId));
    }


}
