package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import java.io.IOException;

public interface CatalogService {
    ServiceResult<String> generateFacebookCatalogCsv();
}
