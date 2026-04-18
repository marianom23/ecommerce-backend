package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductImage;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.service.interfaces.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
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
    public void writeFacebookCatalogCsv(Writer writer) throws IOException {
        List<Product> products = productRepository.findAll();
        
        // Headers requested by USER
        writer.write("id,title,description,availability,condition,price,link,image_link,brand,google_product_category,fb_product_category,quantity_to_sell_on_facebook,sale_price,sale_price_effective_date,item_group_id,gender,color,size,age_group,material,pattern,shipping,shipping_weight,video[0].url,video[0].tag[0],gtin,product_tags[0],product_tags[1],style[0]\n");

        for (Product product : products) {
            if (product.getIsVisible() == null || !product.getIsVisible()) continue;
            
            for (ProductVariant variant : product.getVariants()) {
                writeRow(writer, product, variant);
                writer.write("\n");
            }
        }
        writer.flush();
    }

    private void writeRow(Writer writer, Product product, ProductVariant variant) throws IOException {
        // 1. id (SKU del artículo)
        writer.write(escapeCsv(variant.getSku()));
        writer.write(",");
        
        // 2. title
        writer.write(escapeCsv(product.getName()));
        writer.write(",");
        
        // 3. description
        writer.write(escapeCsv(product.getDescription()));
        writer.write(",");
        
        // 4. availability (in stock; out of stock)
        boolean inStock = (variant.getStock() != null && variant.getStock() > 0) ||
                         (variant.getFulfillmentType() != null && variant.getFulfillmentType().name().startsWith("DIGITAL"));
        writer.write(inStock ? "in stock" : "out of stock");
        writer.write(",");
        
        // 5. condition (new; used)
        writer.write("new");
        writer.write(",");
        
        // 6. price (Format: number followed by 3-letter currency code. Use period for decimal)
        java.math.BigDecimal price = variant.getPrice() != null ? variant.getPrice() : java.math.BigDecimal.ZERO;
        writer.write(price.setScale(2, java.math.RoundingMode.HALF_UP).toString());
        writer.write(" ARS,");
        
        // 7. link (URL de la página del producto corregida con slug)
        String baseUrl = frontBaseUrl;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        writer.write(baseUrl);
        writer.write("/detalle-producto/");
        writer.write(String.valueOf(product.getId()));
        writer.write("-");
        writer.write(toSlug(product.getName()));
        writer.write(",");
        
        // 8. image_link
        String imageUrl = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse("");

        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            // Prepend Cloudinary base (using cloud name found in frontend config)
            imageUrl = "https://res.cloudinary.com/ddyk5hlit/image/upload/" + imageUrl;
        }
        writer.write(escapeCsv(imageUrl));
        writer.write(",");
        
        // 9. brand
        writer.write(escapeCsv(product.getBrand() != null ? product.getBrand().getName() : "Generic"));
        writer.write(",");
        
        // 10. google_product_category
        writer.write(escapeCsv(product.getCategory() != null ? product.getCategory().getName() : ""));
        writer.write(",");
        
        // 11. fb_product_category
        writer.write(escapeCsv(product.getCategory() != null ? product.getCategory().getName() : ""));
        writer.write(",");
        
        // 12. quantity_to_sell_on_facebook
        writer.write(String.valueOf(variant.getStock() != null ? variant.getStock() : 0));
        writer.write(",");
        
        // 13. sale_price (TODO: Logic for discounts if needed)
        writer.write(",");
        
        // 14. sale_price_effective_date
        writer.write(",");
        
        // 15. item_group_id
        writer.write(escapeCsv(product.getSku() != null ? product.getSku() : String.valueOf(product.getId())));
        writer.write(",");
        
        // 16. gender (female; male; unisex)
        writer.write("unisex");
        writer.write(",");
        
        // 17. color
        writer.write(",");
        
        // 18. size
        writer.write(",");
        
        // 19. age_group (adult; all ages; infant; kids; newborn; teen; toddler)
        writer.write("adult");
        writer.write(",");
        
        // 20. material
        writer.write(",");
        
        // 21. pattern
        writer.write(",");
        
        // 22. shipping
        writer.write(",");
        
        // 23. shipping_weight
        writer.write(variant.getWeightKg() != null ? variant.getWeightKg() + " kg" : "");
        writer.write(",");
        
        // 24. video[0].url
        writer.write(",");
        
        // 25. video[0].tag[0]
        writer.write(",");
        
        // 26. gtin
        writer.write(",");
        
        // 27. product_tags[0]
        writer.write(",");
        
        // 28. product_tags[1]
        writer.write(",");
        
        // 29. style[0]
        writer.write("");
    }

    private String toSlug(String input) {
        if (input == null) return "";
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.toLowerCase()
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-", "")
                .replaceAll("-$", "");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        // Clean multi-lines and tabs
        String clean = value.replace("\n", " ")
                            .replace("\r", " ")
                            .replace("\t", " ");
        
        // Limit to reasonable length (FB limit is 9999)
        if (clean.length() > 5000) {
            clean = clean.substring(0, 4997) + "...";
        }

        if (clean.contains(",") || clean.contains("\"")) {
            return "\"" + clean.replace("\"", "\"\"") + "\"";
        }
        return clean;
    }
}
