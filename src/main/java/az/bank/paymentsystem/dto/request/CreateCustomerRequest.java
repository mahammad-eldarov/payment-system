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

    @NotBlank(message = "notBlank.name")
    private String name;

    @NotBlank(message = "notBlank.surname")
    private String surname;

    @NotBlank(message = "notBlank.pin")
    private String pin;

    @NotBlank(message = "notBlank.email")
    @Email(message = "email.message")
    private String email;

    @NotBlank(message = "notBlank.phoneNumber")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "pattern.phoneNumber")
    private String phoneNumber;

}
