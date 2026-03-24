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
import org.springframework.data.domain.Page;
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

    @PostMapping("/external/create")
    @Operation(summary = "Create a new customer",
            description = "Registers a new customer in the system with the provided details.")
    public ResponseEntity<CustomerShortResponse> createCustomer(@RequestBody @Valid CreateCustomerRequest request) {
        CustomerShortResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/external/{customerId}")
    @Operation(summary = "Get customer by ID",
            description = "Retrieves basic information about an active customer using their unique identifier.")
    public ResponseEntity<CustomerShortResponse> getCustomerById(@PathVariable Integer customerId) {
        CustomerShortResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/deleted/{customerId}")
    @Operation(summary = "Get deleted customer by ID",
            description = "Retrieves information about a soft-deleted customer using their unique identifier. Accessible for internal use only.")
    public ResponseEntity<CustomerResponse> getDeletedCustomerById(@PathVariable Integer customerId) {
        CustomerResponse response = customerService.getDeletedCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/internal/status")
//    @Operation(summary = "Get customers by status",
//            description = "Retrieves a list of customers filtered by the specified status (e.g. ACTIVE, BLOCKED, SUSPICIOUS, CLOSED). Accessible for internal use only.")
//    public ResponseEntity<List<CustomerShortResponse>> getCustomersByStatus(
//            @RequestParam CustomerStatus status) {
//
//        return ResponseEntity.ok(customerService.getCustomersByStatus(status));
//    }

    @GetMapping("/internal/status/{status}")
    @Operation(summary = "Get customers by status",
            description = "Retrieves a list of customers filtered by the specified status (e.g. ACTIVE, BLOCKED, SUSPICIOUS, CLOSED). Accessible for internal use only.")
    public ResponseEntity<List<CustomerShortResponse>> getCustomersByStatus(
            @PathVariable CustomerStatus status,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(customerService.getCustomersByStatus(status, page).getContent());
    }

//    @GetMapping("/internal/allcustomers")
//    @Operation(summary = "Get all customers",
//            description = "Retrieves a list of all registered customers in the system. Accessible for internal use only.")
//    public ResponseEntity<List<CustomerShortResponse>> getAllCustomers() {
//        return ResponseEntity.ok(customerService.getAllCustomers());
//    }

    @GetMapping("/internal/allcustomers")
    @Operation(summary = "Get all customers",
            description = "Retrieves a list of all registered customers in the system. Accessible for internal use only.")
    public ResponseEntity<List<CustomerShortResponse>> getAllCustomers(
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(customerService.getAllCustomers(page).getContent());
    }

    @GetMapping("/external/{customerId}/details")
    @Operation(summary = "Get full customer details by ID",
            description = "Retrieves detailed information about a customer including their cards and accounts using their unique identifier.")
    public ResponseEntity<CustomerResponse> getCustomersCardsAndAccounts(@PathVariable Integer customerId) {
        CustomerResponse response = customerService.getCustomersCardsAndAccounts(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/internal/{customerId}/status")
    @Operation(summary = "Update customer status",
            description = "Updates the status of a customer (e.g. ACTIVE, BLOCKED, SUSPICIOUS, CLOSED) using their unique identifier. Accessible for internal use only.")
    public ResponseEntity<MessageResponse> updateCustomerStatus(
            @PathVariable Integer customerId,
            @RequestParam CustomerStatus status) {

        return ResponseEntity.ok(customerService.updateCustomerStatus(customerId, status));
    }

    @PatchMapping("/external/{customerId}")
    @Operation(summary = "Update customer information",
            description = "Updates the personal information of an existing customer using their unique identifier.")
    public ResponseEntity<MessageResponse> updateCustomer(
            @PathVariable Integer customerId,
            @RequestBody @Valid UpdateCustomerRequest request) {

        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @DeleteMapping("/external/{customerId}")
    @Operation(summary = "Delete a customer",
            description = "Soft-deletes a customer by marking their data as invisible. The customer's data is retained in the system but no longer accessible through standard queries.")
    public ResponseEntity<MessageResponse> deleteCustomer(@PathVariable Integer customerId) {

        return ResponseEntity.ok(customerService.deleteCustomer(customerId));
    }

    @GetMapping("/external/{customerId}/cards/{pan}/transactions")
    @Operation(summary = "Get card transactions by PAN.", description = "Returns paginated transactions for a specific card.")
    public ResponseEntity<List<TransactionResponse>> getCardTransactions(
            @PathVariable Integer customerId,
            @PathVariable String pan,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(customerService.getCardTransactions(customerId, pan, page).getContent());
    }

    @GetMapping("/external/{customerId}/accounts/{accountNumber}/transactions")
    @Operation(summary = "Get account transactions.", description = "Returns paginated transactions for a specific account.")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @PathVariable Integer customerId,
            @PathVariable String accountNumber,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(customerService.getAccountTransactions(customerId, accountNumber, page).getContent());
    }


}
