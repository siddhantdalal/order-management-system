package com.orderflow.inventoryservice.service;

import com.orderflow.common.dto.ProductDto;
import com.orderflow.inventoryservice.dto.CreateProductRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    ProductDto createProduct(CreateProductRequest request);
    ProductDto getById(Long id);
    List<ProductDto> getAll();
    List<ProductDto> getByCategory(String category);
    List<ProductDto> search(String query);
    ProductDto updateStock(Long productId, Integer quantity);
    ProductDto uploadImage(Long productId, MultipartFile file) throws IOException;
    ProductDto updateProduct(Long id, CreateProductRequest request);
}
