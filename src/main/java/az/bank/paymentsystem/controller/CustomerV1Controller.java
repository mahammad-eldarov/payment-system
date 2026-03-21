package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.dto.response.CustomerShortResponse;
import az.bank.paymentsystem.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.dto.request.CreateCustomerRequest;
import az.bank.paymentsystem.dto.request.UpdateCustomerRequest;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Controller", description = "Customer Management APIs.")
public class CustomerV1Controller {

    private final CustomerService customerService;

    // POST /api/customers
    @PostMapping
    @Operation(summary = "Create a customer.",
            description = "Creates a new customer.")
    public ResponseEntity<CustomerShortResponse> createCustomer(@RequestBody @Valid CreateCustomerRequest request) {
        CustomerShortResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/customers/{id}
    @GetMapping("/{customerId}")
    @Operation(summary = "Search customer using ID.",
            description = "Search a customer.")
    public ResponseEntity<CustomerShortResponse> getCustomerById(@PathVariable Integer customerId) {
        CustomerShortResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

    // GET /api/customers/deleted/{id}
    @GetMapping("/deleted/{customerId}")
    @Operation(summary = "Search deleted customer using ID.",
            description = "Search a deleted customer.")
    public ResponseEntity<CustomerResponse> getDeletedCustomerById(@PathVariable Integer customerId) {
        CustomerResponse response = customerService.getDeletedCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Get a customer by status.",
            description = "Get customer by status.")
    public ResponseEntity<List<CustomerShortResponse>> getCustomersByStatus(
            @RequestParam CustomerStatus status) {

        return ResponseEntity.ok(customerService.getCustomersByStatus(status));
    }

    // GET /api/customers
    @GetMapping
    @Operation(summary = "Get all customers.",
            description = "Get all customers.")
    public ResponseEntity<List<CustomerShortResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // GET /api/customers/{id}/details
    @GetMapping("/{customerId}/details")
    @Operation(summary = "Search detailed information about customer using ID.",
            description = "Search an detailed information.")
    public ResponseEntity<CustomerResponse> getCustomersCardsAndAccounts(@PathVariable Integer customerId) {
        CustomerResponse response = customerService.getCustomersCardsAndAccounts(customerId);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/customers/{id}/status
    @PatchMapping("/{customerId}/status")
    @Operation(summary = "Update customer status using ID.",
            description = "Update customer status.")
    public ResponseEntity<MessageResponse> updateCustomerStatus(
            @PathVariable Integer customerId,
            @RequestParam CustomerStatus status) {

        return ResponseEntity.ok(customerService.updateCustomerStatus(customerId, status));
    }

    // PATCH /api/customers/{id}
    @PatchMapping("/{customerId}")
    @Operation(summary = "Update all information of a customer using ID.",
            description = "Update all information of a customer.")
    public ResponseEntity<MessageResponse> updateCustomer(
            @PathVariable Integer customerId,
            @RequestBody @Valid UpdateCustomerRequest request) {

        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    // DELETE /api/customers/{id}
    @DeleteMapping("/{customerId}")
    @Operation(summary = "Delete a customer using ID.",
            description = "This method makes the customer's data invisible.")
    public ResponseEntity<MessageResponse> deleteCustomer(@PathVariable Integer customerId) {

        return ResponseEntity.ok(customerService.deleteCustomer(customerId));
    }

//    @GetMapping("/{customerId}/cards-with-transactions")
//    public ResponseEntity<CardTransactionResponse> getCustomerWithCardTransactions(
//            @PathVariable Integer customerId) {
//        return ResponseEntity.ok(customerService.getCustomerWithCardTransactions(customerId));
//    }

//    @GetMapping("/{customerId}/cards-with-transactions")
//    public ResponseEntity<CustomerResponse> getCustomerWithCardTransactions(
//            @PathVariable Integer customerId) {
//        return ResponseEntity.ok(customerService.getCustomerWithCardTransactions(customerId));
//    }

    // Controller
//    @GetMapping("/{customerId}/cards")
//    public ResponseEntity<List<CardForCustomerResponse>> getCustomerCards(
//            @PathVariable Integer customerId) {
//        return ResponseEntity.ok(customerService.getCustomerCards(customerId));
//    }
    @GetMapping("/{customerId}/cards/{pan}/transactions")
    @Operation(summary = "Get card transactions by PAN.", description = "Returns paginated transactions for a specific card.")
    public ResponseEntity<List<TransactionResponse>> getCardTransactions(
            @PathVariable Integer customerId,
            @PathVariable String pan,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(customerService.getCardTransactions(customerId, pan, page).getContent());
    }

    //    @GetMapping("/{customerId}/accounts-with-transactions")
//    public ResponseEntity<CustomerResponse> getCustomerWithAccountTransactions(
//            @PathVariable Integer customerId) {
//        return ResponseEntity.ok(customerService.getCustomerWithAccountTransactions(customerId));
//    }
    @GetMapping("/{customerId}/accounts/{accountNumber}/transactions")
    @Operation(summary = "Get account transactions.", description = "Returns paginated transactions for a specific account.")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @PathVariable Integer customerId,
            @PathVariable String accountNumber,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(customerService.getAccountTransactions(customerId, accountNumber, page).getContent());
    }


}
