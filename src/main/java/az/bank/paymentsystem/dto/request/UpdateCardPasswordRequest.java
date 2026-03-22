package az.bank.paymentsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardPasswordRequest {
    @NotNull(message = "notNull.password")
    @Pattern(regexp = "^[0-9]{4}$", message = "pattern.password")
    private String password;

}
