// src/main/java/com/empresa/ecommerce_backend/model/embeddable/AddressSnapshot.java
package com.empresa.ecommerce_backend.model.embeddable;

import jakarta.persistence.*;
import lombok.*;

// src/main/java/com/empresa/ecommerce_backend/model/embeddable/AddressSnapshot.java
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddressSnapshot {
    @Column(length = 150 /*, nullable = false */) private String street;
    @Column(length = 20)  private String streetNumber;
    @Column(length = 100 /*, nullable = false */) private String city;
    @Column(length = 100) private String state;
    @Column(length = 20)  private String postalCode;
    @Column(length = 100 /*, nullable = false */) private String country;
    @Column(length = 20)  private String apartmentNumber;
    @Column(length = 10)  private String floor;
    @Column(length = 50)  private String recipientName;
    @Column(length = 20)  private String phone;
}
