package com.orderflow.inventoryservice.controller;

import com.orderflow.common.dto.ApiResponse;
import com.orderflow.common.dto.ProductDto;
import com.orderflow.inventoryservice.dto.UpdateStockRequest;
import com.orderflow.inventoryservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getStockInfo(@PathVariable Long productId) {
        ProductDto product = productService.getById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<ProductDto>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request) {
        ProductDto product = productService.updateStock(productId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Stock updated", product));
    }
}
