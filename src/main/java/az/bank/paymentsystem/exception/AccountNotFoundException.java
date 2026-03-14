package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.NotFoundException;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(String message) { super(message); }
}


//package org.example.exampletask.exception;
//
//public class AccountNotFoundException extends BaseException {
//    public AccountNotFoundException(Integer id) {
//        super(
//                "Cari hesab tapılmadı. ID: " + id,
//                "ACCOUNT_NOT_FOUND",
//                404
//        );
//    }
//}



