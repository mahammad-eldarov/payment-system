package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class CardAlreadyCancelledException extends BadRequestException {
    public CardAlreadyCancelledException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class CardAlreadyCancelledException extends BaseException {
//    public CardAlreadyCancelledException() {
//        super(
//                "Bu kart ləğv edilib. Ödəniş mümkün deyil.",
//                "CARD_CANCELLED",
//                400
//        );
//    }
//}
