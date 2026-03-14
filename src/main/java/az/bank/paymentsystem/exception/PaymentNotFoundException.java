package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class PaymentNotFoundException extends NotFoundException {
    public PaymentNotFoundException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class PaymentNotFoundException extends BaseException {
//    public PaymentNotFoundException(Integer id) {
//        super(
//                "Ödəniş tapılmadı. ID: " + id,
//                "PAYMENT_NOT_FOUND",
//                404
//        );
//    }
//}
