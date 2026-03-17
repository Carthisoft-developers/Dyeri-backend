// com/cuisinvoisin/domain/exceptions/ResourceNotFoundException.java
package com.cuisinvoisin.domain.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }
}
