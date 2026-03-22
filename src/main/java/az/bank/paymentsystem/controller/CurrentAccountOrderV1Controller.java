package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.dto.response.CurrentAccountOrderResponse;
import az.bank.paymentsystem.service.CurrentAccountOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/current-account-order")
@RequiredArgsConstructor
@Tag(name = "Current Account Order Controller", description = "Current account order management")
public class CurrentAccountOrderV1Controller {

    private final CurrentAccountOrderService currentAccountOrderRequestService;

    @PostMapping("/customer/{customerId}")
    @Operation(summary = "Order a current account", description = "Creates a current account order for a customer")
    public ResponseEntity<CurrentAccountOrderResponse> orderAccount(
            @PathVariable Integer customerId,
            @RequestBody @Valid OrderCurrentAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(currentAccountOrderRequestService.orderCurrentAccount(customerId, request));
    }

}
