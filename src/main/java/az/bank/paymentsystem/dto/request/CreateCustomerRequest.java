package az.bank.paymentsystem.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotBlank(message = "Name cannot be empty.")
    private String name;

    @NotBlank(message = "Surname cannot be empty.")
    private String surname;

    @NotBlank(message = "Pin cannot be empty.")
    private String pin;

    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "The email format is incorrect.")
    private String email;

    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "The phone number format should be like this (+994XXXXXXXXX).")
    private String phoneNumber;

}
