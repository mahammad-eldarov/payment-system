package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class CardLimitExceededException extends BadRequestException {
    public CardLimitExceededException(String message) { super(message); }
}

//package org.example.exampletask.exception;
//
//public class CardLimitExceededException extends BaseException {
//    public CardLimitExceededException() {
//        super(
//                "Müştərinin artıq 2 kartı mövcuddur. Yeni kart sifariş edilə bilməz.",
//                "CARD_LIMIT_EXCEEDED",
//                400
//        );
//    }
//}

