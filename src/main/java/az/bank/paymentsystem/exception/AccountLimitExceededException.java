package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.BadRequestException;

public class AccountLimitExceededException extends BadRequestException {
    public AccountLimitExceededException(String message) { super(message); }
}



//package org.example.exampletask.exception;
//
//public class AccountLimitExceededException extends BaseException{
//    public AccountLimitExceededException() {
//        super(
//                "Müştərinin artıq 3 cari hesabı mövcuddur. Yeni hesab sifariş edilə bilməz.",
//                "ACCOUNT_LIMIT_EXCEEDED",
//                400
//        );
//    }
//}
