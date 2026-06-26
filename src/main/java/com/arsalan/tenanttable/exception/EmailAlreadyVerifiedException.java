package com.arsalan.tenanttable.exception;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException(String s) {
        super(s);
    }
}
