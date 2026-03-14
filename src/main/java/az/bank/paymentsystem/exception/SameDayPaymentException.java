package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class SameDayPaymentException extends BadRequestException {
    public SameDayPaymentException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class SameDayPaymentException extends BaseException {
//    public SameDayPaymentException() {
//        super(
//                "Müştəri eyni gün həm kartdan həm də cari hesabdan ödəniş edə bilməz.",
//                "SAME_DAY_PAYMENT_CONFLICT",
//                400
//        );
//    }
//
//}
