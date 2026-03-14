package az.bank.paymentsystem.exception;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(String message) {
        super(message);
    }
}



//package org.example.exampletask.exception;
//
//public class InsufficientBalanceException extends BaseException{
//    public InsufficientBalanceException(String sourceType) {
//        super(
//                sourceType.equals("CARD")
//                        ? "Kart balansı 10 AZN-dən aşağıdır. Ödəniş mümkün deyil."
//                        : "Cari hesab balansı 5 USD-dən aşağıdır. Ödəniş mümkün deyil.",
//                "INSUFFICIENT_BALANCE",
//                400
//        );
//    }
//}
