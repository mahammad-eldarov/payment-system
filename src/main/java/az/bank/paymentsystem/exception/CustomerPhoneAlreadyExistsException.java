package az.bank.paymentsystem.exception;

import az.bank.paymentsystem.exception.base.ConflictException;

public class CustomerPhoneAlreadyExistsException extends ConflictException {
    public CustomerPhoneAlreadyExistsException(String message) { super(message); }
}



//package org.example.exampletask.exception;
//
//public class CustomerPhoneAlreadyExistsException extends BaseException {
//    public CustomerPhoneAlreadyExistsException(String phone) {
//        super(
//                "Telefon tapılmadı. Phone: " + phone,
//                "CUSTOMER_NOT_FOUND",
//                404
//        );
//    }
//
//}
