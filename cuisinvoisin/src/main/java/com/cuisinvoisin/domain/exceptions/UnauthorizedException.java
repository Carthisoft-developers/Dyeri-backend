// com/cuisinvoisin/domain/exceptions/UnauthorizedException.java
package com.cuisinvoisin.domain.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
