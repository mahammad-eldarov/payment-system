package az.bank.paymentsystem.client;

import az.bank.paymentsystem.dto.response.CbarResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cbar-client", url = "${cbar.base-url:https://www.cbar.az}")
public interface CbarCurrencyClient {

    @GetMapping(value = "/currencies/{date}.xml", produces = MediaType.APPLICATION_XML_VALUE)
    CbarResponse getRates(@PathVariable("date") String date);
}
