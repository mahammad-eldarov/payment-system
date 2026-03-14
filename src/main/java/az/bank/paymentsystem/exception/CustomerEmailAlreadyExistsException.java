package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.ConflictException;

public class CustomerEmailAlreadyExistsException extends ConflictException {
    public CustomerEmailAlreadyExistsException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class CustomerEmailAlreadyExistsException extends BaseException{
//    public CustomerEmailAlreadyExistsException(String email) {
//        super(
//                "Email tapılmadı. Email: " + email,
//                "CUSTOMER_NOT_FOUND",
//                404
//        );
//    }
//
//}

