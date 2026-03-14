package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class EmptyListException extends NotFoundException {
    public EmptyListException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class EmptyListException extends BaseException{
//    public EmptyListException (String message) {
//        super(
//                "Message: " + message,
//                "LIST_IS_EMPTY",
//                404
//        );
//    }
//}
