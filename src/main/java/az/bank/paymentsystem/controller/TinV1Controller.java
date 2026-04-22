package az.bank.paymentsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.TinResponse;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.service.TinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tin")
@RequiredArgsConstructor
@Tag(name = "TIN Controller", description = "TIN Management APIs.")
public class TinV1Controller {

    private final TinService tinService;


    @PatchMapping("/internal/{tinId}/status")
    @Operation(summary = "Update TIN status using ID.",
            description = "Update TIN status.")
    public ResponseEntity<MessageResponse> updateTinStatus(
            @PathVariable Integer tinId,
            @RequestParam TinStatus status) {

        return ResponseEntity.ok(tinService.updateTinStatus(tinId, status));
    }

    @GetMapping("/external/customer/{customerId}")
    @Operation(summary = "Get TINs by customer ID.", description = "Retrieves all TINs for a customer.")
    public ResponseEntity<List<TinResponse>> getTinByCustomerId(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(tinService.getTinByCustomerId(customerId));
    }

    @GetMapping("/external/{tinNumber}")
    @Operation(summary = "Get TIN by TIN number.", description = "Retrieves a TIN by its TIN number.")
    public ResponseEntity<TinResponse> getTinByTinNumber(
            @PathVariable String tinNumber) {
        return ResponseEntity.ok(tinService.getTinByTinNumber(tinNumber));
    }

    @GetMapping("/internal/status/{status}")
    @Operation(summary = "Get a TIN status.", description = "Get TIN by status.")
    public ResponseEntity<List<TinResponse>> getTinByStatus(
            @PathVariable TinStatus status,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(tinService.getTinByStatus(status, page).getContent());
    }

    @DeleteMapping("/external/{tinId}/delete")
    @Operation(summary = "Delete a TIN.", description = "Soft-deletes a TIN using its ID.")
    public ResponseEntity<MessageResponse> deleteTin(@PathVariable Integer tinId) {

        return ResponseEntity.ok(tinService.deleteTin(tinId));
    }
}
