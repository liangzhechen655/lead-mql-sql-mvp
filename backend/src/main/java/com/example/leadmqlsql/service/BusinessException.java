package com.example.leadmqlsql.service;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
