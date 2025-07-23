// src/main/java/com/empresa/ecommerce_backend/service/CrudService.java
package com.empresa.ecommerce_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CrudService<RES, ID, CREATE_REQ, UPDATE_REQ> {
    RES create(CREATE_REQ req);
    RES update(ID id, UPDATE_REQ req);
    void delete(ID id);
    RES get(ID id);
    Page<RES> list(Pageable pageable);
}
