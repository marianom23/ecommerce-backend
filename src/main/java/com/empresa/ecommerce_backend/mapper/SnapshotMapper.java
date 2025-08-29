package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.model.Address;
import com.empresa.ecommerce_backend.model.BillingProfile;
import com.empresa.ecommerce_backend.model.embeddable.AddressSnapshot;
import com.empresa.ecommerce_backend.model.embeddable.BillingSnapshot;

public final class SnapshotMapper {

    private SnapshotMapper() {}

    public static AddressSnapshot toSnapshot(Address a) {
        if (a == null) return null;
        AddressSnapshot s = new AddressSnapshot();
        s.setStreet(a.getStreet());
        s.setStreetNumber(a.getStreetNumber());
        s.setCity(a.getCity());
        s.setState(a.getState());
        s.setPostalCode(a.getPostalCode());
        s.setCountry(a.getCountry());
        s.setApartmentNumber(a.getApartmentNumber());
        s.setFloor(a.getFloor());
        // Si tu Address tuviera recipientName/phone, setéalos acá:
        // s.setRecipientName(a.getRecipientName());
        // s.setPhone(a.getPhone());
        return s;
    }

    public static BillingSnapshot toSnapshot(BillingProfile bp, Address billingAddr) {
        if (bp == null || billingAddr == null) return null;
        BillingSnapshot s = new BillingSnapshot();

        // 🔹 Datos fiscales/personales del perfil de facturación
        s.setDocumentType(bp.getDocumentType());
        s.setDocumentNumber(bp.getDocumentNumber());
        s.setTaxCondition(bp.getTaxCondition());
        s.setBusinessName(bp.getBusinessName());
        s.setEmailForInvoices(bp.getEmailForInvoices());
        s.setPhone(bp.getPhone());

        // 🔹 Dirección fiscal (snapshot de Address)
        s.setStreet(billingAddr.getStreet());
        s.setStreetNumber(billingAddr.getStreetNumber());
        s.setCity(billingAddr.getCity());
        s.setState(billingAddr.getState());
        s.setPostalCode(billingAddr.getPostalCode());
        s.setCountry(billingAddr.getCountry());
        s.setApartmentNumber(billingAddr.getApartmentNumber());
        s.setFloor(billingAddr.getFloor());

        return s;
    }
}
