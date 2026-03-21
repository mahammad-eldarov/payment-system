package az.bank.paymentsystem.dto.response;

import az.bank.paymentsystem.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerShortResponse {
    private Integer id;
    private String name;
    private String surname;
    private String pin;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;
}

