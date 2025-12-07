package com.enterprise.shop.backend.service;

import com.enterprise.shop.backend.model.Product;
import com.enterprise.shop.backend.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @PostConstruct
    public void initSampleProducts() {
        if (productRepository.count() == 0) {
            List<Product> products = List.of(
                Product.builder()
                    .name("Classic T-Shirt")
                    .description("Comfortable cotton t-shirt, perfect for everyday wear")
                    .price(new BigDecimal("29.99"))
                    .imageUrl("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400")
                    .category("Clothing")
                    .stockQuantity(100)
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Slim Fit Jeans")
                    .description("Modern slim fit denim jeans with stretch comfort")
                    .price(new BigDecimal("79.99"))
                    .imageUrl("https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=400")
                    .category("Clothing")
                    .stockQuantity(75)
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Wireless Headphones")
                    .description("Premium wireless headphones with noise cancellation")
                    .price(new BigDecimal("199.99"))
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400")
                    .category("Electronics")
                    .stockQuantity(50)
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Smart Watch")
                    .description("Feature-rich smartwatch with health monitoring")
                    .price(new BigDecimal("299.99"))
                    .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400")
                    .category("Electronics")
                    .stockQuantity(40)
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Running Shoes")
                    .description("Lightweight running shoes with superior cushioning")
                    .price(new BigDecimal("129.99"))
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400")
                    .category("Footwear")
                    .stockQuantity(60)
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Leather Backpack")
                    .description("Stylish leather backpack with laptop compartment")
                    .price(new BigDecimal("149.99"))
                    .imageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400")
                    .category("Accessories")
                    .stockQuantity(35)
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Sunglasses")
                    .description("UV protection polarized sunglasses")
                    .price(new BigDecimal("89.99"))
                    .imageUrl("https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=400")
                    .category("Accessories")
                    .stockQuantity(80)
                    .active(true)
                    .build(),
                Product.builder()
                    .name("Coffee Maker")
                    .description("Programmable coffee maker with thermal carafe")
                    .price(new BigDecimal("79.99"))
                    .imageUrl("https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=400")
                    .category("Home")
                    .stockQuantity(45)
                    .active(true)
                    .build()
            );
            productRepository.saveAll(products);
        }
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
}
