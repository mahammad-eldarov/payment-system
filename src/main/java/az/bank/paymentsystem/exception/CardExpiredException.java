package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class CardExpiredException extends BadRequestException {
    public CardExpiredException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class CardExpiredException extends BaseException{
//    public CardExpiredException() {
//        super(
//                "Kartın istifadə müddəti bitib. Ödəniş mümkün deyil.",
//                "CARD_EXPIRED",
//                400
//        );
//    }
//}

