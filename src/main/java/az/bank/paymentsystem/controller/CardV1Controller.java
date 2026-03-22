package az.bank.paymentsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.request.UpdateCardPasswordRequest;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.dto.request.OrderCardRequest;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.service.CardService;
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
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card Controller", description = "Card Management APIs.")
public class CardV1Controller {

    private final CardService cardService;

    @PatchMapping("/{cardId}/status")
    @Operation(summary = "Update card status using ID.",
            description = "Update card status.")
    public ResponseEntity<MessageResponse> updateCardStatus(
            @PathVariable Integer cardId,
            @RequestParam CardStatus status) {

        return ResponseEntity.ok(cardService.updateCardStatus(cardId, status));

    }

    @PatchMapping("/password/card/{cardId}")
    @Operation(summary = "Update card password using card ID.",
            description = "Update card password.")
    public ResponseEntity<MessageResponse> updateCardPassword(
            @PathVariable Integer cardId, UpdateCardPasswordRequest request) {

        return ResponseEntity.ok(cardService.updateCardPassword(cardId, request));

    }

    @DeleteMapping("/{cardId}/delete")
    @Operation(summary = "Delete a card.",
            description = "Delete a card using card id.")
    public ResponseEntity<MessageResponse> deleteCard(@PathVariable Integer cardId) {

        return ResponseEntity.ok(cardService.deleteCard(cardId));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Search card using ID.",
            description = "Search a card.")
    public ResponseEntity<List<CardResponse>> getCardsByCustomerId(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(cardService.getCardsByCustomerId(customerId));
    }

    @GetMapping("/{pan}")
    @Operation(summary = "Search card using PAN.",
            description = "Search a card pan.")
    public ResponseEntity<CardResponse> getCardByPan(@PathVariable String pan) {
        return ResponseEntity.ok(cardService.getCardByPan(pan));
    }

    @GetMapping("/status")
    @Operation(summary = "Get card by status.",
            description = "Search a card status.")
    public ResponseEntity<List<CardResponse>> getCardsByStatus(
            @RequestParam CardStatus status) {

        return ResponseEntity.ok(cardService.getCardsByStatus(status));
    }
}

