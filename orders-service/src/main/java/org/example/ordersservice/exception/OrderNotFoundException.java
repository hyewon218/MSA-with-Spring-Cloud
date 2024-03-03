package org.example.ordersservice.exception;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}