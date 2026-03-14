package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class AccountExpiredException extends BadRequestException {
    public AccountExpiredException(String message) { super(message); }
}


//public class AccountExpiredException extends BaseException {
//    public AccountExpiredException() {
//        super(
//                "Cari hesabın istifadə müddəti bitib. Ödəniş mümkün deyil.",
//                "ACCOUNT_EXPIRED",
//                400
//        );
//    }
//}
