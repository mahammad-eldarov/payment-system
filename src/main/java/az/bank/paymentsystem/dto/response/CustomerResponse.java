package az.bank.paymentsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.bank.paymentsystem.enums.CustomerStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponse {

    private Integer id;
    private String name;
    private String surname;
    private String pin;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;

    private String cardMessage;
    private String accountMessage;

    private List<CardResponse> cardResponse;
    private List<CurrentAccountResponse> currentAccountResponse;

}
