// src/main/java/com/empresa/ecommerce_backend/events/UserRegisteredEvent.java
package com.empresa.ecommerce_backend.events;

public record UserRegisteredEvent(String email, String token) {}
