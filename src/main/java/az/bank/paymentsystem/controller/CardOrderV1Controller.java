package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.service.CardOrderService;
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
@RequestMapping("/api/v1/card-order")
@RequiredArgsConstructor
@Tag(name = "Card Order Controller", description = "Card order management")
public class CardOrderV1Controller {

    private final CardOrderService cardOrderRequestService;

    @PostMapping("/customer/{customerId}")
    @Operation(summary = "Order a card", description = "Creates a card order for a customer")
    public ResponseEntity<CardOrderResponse> orderCard(
            @PathVariable Integer customerId,
            @RequestBody @Valid OrderCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardOrderRequestService.orderCard(customerId, request));
    }
}
