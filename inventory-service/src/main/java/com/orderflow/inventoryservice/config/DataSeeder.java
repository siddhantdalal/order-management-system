package com.orderflow.inventoryservice.config;

import com.orderflow.inventoryservice.entity.Category;
import com.orderflow.inventoryservice.entity.Product;
import com.orderflow.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("Products already seeded, skipping...");
            return;
        }

        List<Product> products = List.of(
                product("MacBook Pro 16\"", "Apple M3 Pro chip, 18GB RAM, 512GB SSD", "2499.99", 25, Category.ELECTRONICS),
                product("Sony WH-1000XM5", "Industry-leading noise canceling wireless headphones", "349.99", 50, Category.ELECTRONICS),
                product("Samsung Galaxy S24 Ultra", "6.8\" Dynamic AMOLED, 200MP camera, 5000mAh battery", "1299.99", 35, Category.ELECTRONICS),
                product("Dell UltraSharp 27\" Monitor", "4K UHD, IPS, USB-C Hub Monitor", "549.99", 20, Category.ELECTRONICS),
                product("Logitech MX Master 3S", "Advanced wireless mouse with quiet clicks", "99.99", 100, Category.ELECTRONICS),
                product("Nike Air Max 270", "Men's running shoes with Air cushioning", "149.99", 75, Category.CLOTHING),
                product("Levi's 501 Original Jeans", "Classic straight fit jeans in dark wash", "69.99", 120, Category.CLOTHING),
                product("North Face Puffer Jacket", "Thermoball eco-insulated winter jacket", "229.99", 40, Category.CLOTHING),
                product("Clean Code", "A Handbook of Agile Software Craftsmanship by Robert C. Martin", "39.99", 200, Category.BOOKS),
                product("Designing Data-Intensive Applications", "The big ideas behind reliable systems by Martin Kleppmann", "44.99", 150, Category.BOOKS),
                product("System Design Interview", "An insider's guide by Alex Xu", "36.99", 180, Category.BOOKS),
                product("Instant Pot Duo 7-in-1", "Electric pressure cooker, 6 quart", "89.99", 60, Category.HOME),
                product("Dyson V15 Detect", "Cordless stick vacuum with laser dust detection", "749.99", 15, Category.HOME),
                product("KitchenAid Stand Mixer", "Artisan series 5-quart tilt-head stand mixer", "379.99", 30, Category.HOME),
                product("Yoga Mat Premium", "Non-slip exercise mat, 6mm thick, with carrying strap", "34.99", 200, Category.SPORTS),
                product("Adjustable Dumbbell Set", "5-52.5 lbs adjustable weight set per dumbbell", "349.99", 25, Category.SPORTS),
                product("Wilson Evolution Basketball", "Official size game basketball, indoor use", "64.99", 80, Category.SPORTS),
                product("Raspberry Pi 5", "8GB RAM, quad-core ARM processor", "79.99", 45, Category.ELECTRONICS),
                product("Patagonia Better Sweater", "Full-zip fleece jacket, recycled polyester", "139.99", 55, Category.CLOTHING),
                product("Atomic Habits", "An easy & proven way to build good habits by James Clear", "16.99", 300, Category.BOOKS)
        );

        productRepository.saveAll(products);
        log.info("Seeded {} products into the database", products.size());
    }

    private Product product(String name, String description, String price, int stock, Category category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .active(true)
                .build();
    }
}
