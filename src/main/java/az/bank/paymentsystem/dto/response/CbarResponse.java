package az.bank.paymentsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "ValCurs")
public class CbarResponse {

    @JacksonXmlProperty(isAttribute = true, localName = "Date")
    private String date;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "ValType")
    private List<ValType> valTypes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValType {

        @JacksonXmlProperty(isAttribute = true, localName = "Type")
        private String type;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Valute")
        private List<Valute> valuteList;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Valute {

        @JacksonXmlProperty(isAttribute = true, localName = "Code")
        private String code;

        @JacksonXmlProperty(localName = "Value")
        private BigDecimal value;

        @JacksonXmlProperty(localName = "Nominal")
        private String nominal;
    }
}
