package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import java.io.IOException;

import java.io.Writer;

public interface CatalogService {
    void writeFacebookCatalogCsv(Writer writer) throws IOException;
}
