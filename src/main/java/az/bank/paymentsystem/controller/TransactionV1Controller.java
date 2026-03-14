package az.bank.paymentsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Controller", description = "Transaction Management APIs.")
public class TransactionV1Controller {

    private final TransactionService transactionService;

    @GetMapping("/card/{cardId}")
    @Operation(summary = "Get last 100 transactions by card ID.",
            description = "Returns transactions where card is sender or receiver.")
    public ResponseEntity<List<TransactionResponse>> getByCardId(
            @PathVariable Integer cardId,
            @RequestParam(required = false, defaultValue = "0") int page) {
        return ResponseEntity.ok(transactionService.getTransactionsByCardId(cardId, page).getContent());
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get last 100 transactions by account ID.",
            description = "Returns transactions where account is sender or receiver.")
    public ResponseEntity<List<TransactionResponse>> getByAccountId(
            @PathVariable Integer accountId,
            @RequestParam(required = false, defaultValue = "0") int page) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId, page).getContent());
    }

    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "Get transactions by payment ID.",
            description = "Returns all transactions linked to a payment.")
    public ResponseEntity<List<TransactionResponse>> getByPaymentId(
            @PathVariable Integer paymentId,
            @RequestParam(required = false, defaultValue = "0") int page) {
        return ResponseEntity.ok(transactionService.getTransactionsByPaymentId(paymentId, page).getContent());
    }

}
