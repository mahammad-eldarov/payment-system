package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class CardNotFoundException extends NotFoundException {
    public CardNotFoundException(String message) { super(message); }
}

//package org.example.exampletask.exception;
//
//public class CardNotFoundException extends BaseException {
//    public CardNotFoundException(Integer id) {
//        super(
//                "Kart tapılmadı. ID: " + id,
//                "CARD_NOT_FOUND",
//                404
//        );
//    }
//}


