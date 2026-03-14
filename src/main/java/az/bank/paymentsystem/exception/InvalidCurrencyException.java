package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class InvalidCurrencyException extends BadRequestException {
    public InvalidCurrencyException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class InvalidCurrencyException extends BaseException{
//    public InvalidCurrencyException(String currency) {
//        super(
//                "Dəstəklənməyən valyuta: " + currency + ". Yalnız AZN, USD, EUR qəbul edilir.",
//                "INVALID_CURRENCY",
//                400
//        );
//    }
//}
