package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductImage;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.service.interfaces.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepository;

    @Value("${front.base-url:http://localhost:3000}")
    private String frontBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<String> generateFacebookCatalogCsv() {
        List<Product> products = productRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        // Headers requested by USER
        csv.append("id,title,description,availability,condition,price,link,image_link,brand,google_product_category,fb_product_category,quantity_to_sell_on_facebook,sale_price,sale_price_effective_date,item_group_id,gender,color,size,age_group,material,pattern,shipping,shipping_weight,video[0].url,video[0].tag[0],gtin,product_tags[0],product_tags[1],style[0]\n");

        for (Product product : products) {
            if (product.getIsVisible() == null || !product.getIsVisible()) continue;
            
            for (ProductVariant variant : product.getVariants()) {
                appendRow(csv, product, variant);
                csv.append("\n");
            }
        }

        return ServiceResult.ok(csv.toString());
    }

    private void appendRow(StringBuilder csv, Product product, ProductVariant variant) {
        // 1. id (SKU del artículo)
        csv.append(escapeCsv(variant.getSku())).append(",");
        
        // 2. title
        csv.append(escapeCsv(product.getName())).append(",");
        
        // 3. description
        csv.append(escapeCsv(product.getDescription())).append(",");
        
        // 4. availability (in stock; out of stock)
        csv.append(variant.getStock() != null && variant.getStock() > 0 ? "in stock" : "out of stock").append(",");
        
        // 5. condition (new; used)
        csv.append("new").append(",");
        
        // 6. price (Format: number followed by 3-letter currency code. Use period for decimal)
        csv.append(variant.getPrice()).append(" USD,");
        
        // 7. link (URL de la página del producto)
        csv.append(frontBaseUrl).append("/product/").append(product.getId()).append(",");
        
        // 8. image_link
        String imageUrl = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse("");
        csv.append(escapeCsv(imageUrl)).append(",");
        
        // 9. brand
        csv.append(escapeCsv(product.getBrand() != null ? product.getBrand().getName() : "Generic")).append(",");
        
        // 10. google_product_category
        csv.append(escapeCsv(product.getCategory() != null ? product.getCategory().getName() : "")).append(",");
        
        // 11. fb_product_category
        csv.append(escapeCsv(product.getCategory() != null ? product.getCategory().getName() : "")).append(",");
        
        // 12. quantity_to_sell_on_facebook
        csv.append(variant.getStock() != null ? variant.getStock() : 0).append(",");
        
        // 13. sale_price (TODO: Logic for discounts if needed)
        csv.append(",");
        
        // 14. sale_price_effective_date
        csv.append(",");
        
        // 15. item_group_id
        csv.append(escapeCsv(product.getSku() != null ? product.getSku() : String.valueOf(product.getId()))).append(",");
        
        // 16. gender (female; male; unisex)
        csv.append("unisex").append(",");
        
        // 17. color
        csv.append(",");
        
        // 18. size
        csv.append(",");
        
        // 19. age_group (adult; all ages; infant; kids; newborn; teen; toddler)
        csv.append("adult").append(",");
        
        // 20. material
        csv.append(",");
        
        // 21. pattern
        csv.append(",");
        
        // 22. shipping
        csv.append(",");
        
        // 23. shipping_weight
        csv.append(variant.getWeightKg() != null ? variant.getWeightKg() + " kg" : "").append(",");
        
        // 24. video[0].url
        csv.append(",");
        
        // 25. video[0].tag[0]
        csv.append(",");
        
        // 26. gtin
        csv.append(",");
        
        // 27. product_tags[0]
        csv.append(",");
        
        // 28. product_tags[1]
        csv.append(",");
        
        // 29. style[0]
        csv.append("");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String clean = value.replace("\n", " ").replace("\r", " ");
        if (clean.contains(",") || clean.contains("\"")) {
            return "\"" + clean.replace("\"", "\"\"") + "\"";
        }
        return clean;
    }
}
