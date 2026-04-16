package com.orderflow.inventoryservice.service;

import com.orderflow.common.dto.ProductDto;
import com.orderflow.common.exception.ResourceNotFoundException;
import com.orderflow.inventoryservice.dto.CreateProductRequest;
import com.orderflow.inventoryservice.entity.Category;
import com.orderflow.inventoryservice.entity.Product;
import com.orderflow.inventoryservice.mapper.ProductMapper;
import com.orderflow.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final S3Service s3Service;

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(Category.valueOf(request.getCategory()))
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: id={}, name={}", saved.getId(), saved.getName());
        return productMapper.toDto(saved);
    }

    @Override
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductDto getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return productMapper.toDto(product);
    }

    @Override
    public List<ProductDto> getAll() {
        return productRepository.findByActiveTrue().stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> getByCategory(String category) {
        Category cat = Category.valueOf(category.toUpperCase());
        return productRepository.findByCategoryAndActiveTrue(cat).stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> search(String query) {
        return productRepository.search(query).stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductDto updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        product.setStockQuantity(quantity);
        Product saved = productRepository.save(product);
        log.info("Stock updated: productId={}, newQuantity={}", productId, quantity);
        return productMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductDto uploadImage(Long productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        String imageUrl = s3Service.uploadFile(file);
        product.setImageUrl(imageUrl);
        Product saved = productRepository.save(product);
        log.info("Image uploaded for product: id={}, url={}", productId, imageUrl);
        return productMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductDto updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(Category.valueOf(request.getCategory()));

        Product saved = productRepository.save(product);
        log.info("Product updated: id={}", id);
        return productMapper.toDto(saved);
    }
}
