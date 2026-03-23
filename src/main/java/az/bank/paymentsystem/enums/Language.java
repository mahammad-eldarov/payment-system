package az.bank.paymentsystem.enums;
import java.util.Locale;

public enum Language {
    AZ,
    EN;

    public Locale toLocale() {
        return Locale.forLanguageTag(this.name().toLowerCase());
    }

}
