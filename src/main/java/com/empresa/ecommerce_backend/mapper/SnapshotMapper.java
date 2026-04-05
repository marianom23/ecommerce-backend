package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.UpdateBillingProfileRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateShippingAddressRequest;
import com.empresa.ecommerce_backend.model.Address;
import com.empresa.ecommerce_backend.model.BillingProfile;
import com.empresa.ecommerce_backend.model.embeddable.AddressSnapshot;
import com.empresa.ecommerce_backend.model.embeddable.BillingSnapshot;

public final class SnapshotMapper {

    private SnapshotMapper() {
    }

    public static AddressSnapshot toSnapshot(Address a) {
        if (a == null)
            return null;
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

    public static BillingSnapshot toSnapshot(BillingProfile bp) {
        if (bp == null)
            return null;
        BillingSnapshot s = new BillingSnapshot();

        s.setFullName(bp.getFullName()); // ✅ nuevo

        s.setDocumentType(bp.getDocumentType());
        s.setDocumentNumber(bp.getDocumentNumber());
        s.setTaxCondition(bp.getTaxCondition());
        s.setBusinessName(bp.getBusinessName());
        s.setEmailForInvoices(bp.getEmailForInvoices());
        s.setPhone(bp.getPhone());

        s.setStreet(bp.getStreet());
        s.setStreetNumber(bp.getStreetNumber());
        s.setCity(bp.getCity());
        s.setState(bp.getState());
        s.setPostalCode(bp.getPostalCode());
        s.setCountry(bp.getCountry());
        s.setApartmentNumber(bp.getApartmentNumber());
        s.setFloor(bp.getFloor());

        return s;
    }

    // === Guest checkout: crear snapshots desde requests ===

    public static AddressSnapshot fromRequest(UpdateShippingAddressRequest req) {
        if (req == null)
            return null;
        AddressSnapshot s = new AddressSnapshot();
        s.setStreet(req.getStreet());
        s.setStreetNumber(req.getStreetNumber());
        s.setCity(req.getCity());
        s.setState(req.getState());
        s.setPostalCode(req.getPostalCode());
        s.setCountry(req.getCountry());
        s.setApartmentNumber(req.getApartmentNumber());
        s.setFloor(req.getFloor());
        s.setRecipientName(req.getRecipientName());
        s.setPhone(req.getPhone());
        return s;
    }

    public static BillingSnapshot fromRequest(UpdateBillingProfileRequest req) {
        if (req == null)
            return null;
        BillingSnapshot s = new BillingSnapshot();
        s.setFullName(req.getFullName());
        s.setEmailForInvoices(req.getEmail());
        s.setPhone(req.getPhone());
        
        s.setDocumentType(req.getDocumentType());
        s.setDocumentNumber(req.getDocumentNumber());
        s.setTaxCondition(req.getTaxCondition());
        s.setBusinessName(req.getBusinessName());
        
        // Dirección fiscal (opcional para guests con productos digitales)
        s.setStreet(req.getStreet());
        s.setStreetNumber(req.getStreetNumber());
        s.setCity(req.getCity());
        s.setState(req.getState());
        s.setPostalCode(req.getPostalCode());
        s.setCountry(req.getCountry());
        s.setApartmentNumber(req.getApartmentNumber());
        s.setFloor(req.getFloor());
        return s;
    }
}
