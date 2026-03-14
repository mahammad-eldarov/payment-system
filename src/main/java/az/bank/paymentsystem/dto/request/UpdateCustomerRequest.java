package az.bank.paymentsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    private String name;

    private String surname;

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "The phone number format should be like this (+994XXXXXXXXX).")
    private String phoneNumber;
}
