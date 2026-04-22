package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.request.OrderTinRequest;
import az.bank.paymentsystem.dto.response.TinOrderResponse;
import az.bank.paymentsystem.service.TinOrderService;
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
@RequestMapping("/api/v1/tin-order")
@RequiredArgsConstructor
@Tag(name = "TIN Order Controller", description = "TIN order management")
public class TinOrderV1Controller {

    private final TinOrderService tinOrderRequestService;

    @PostMapping("/external/customer/{customerId}")
    @Operation(summary = "Order a TIN", description = "Creates a TIN order for a customer")
    public ResponseEntity<TinOrderResponse> orderTin(
            @PathVariable Integer customerId,
            @RequestBody @Valid OrderTinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tinOrderRequestService.orderTin(customerId, request));
    }

}
