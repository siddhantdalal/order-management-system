package com.orderflow.inventoryservice.service;

import com.orderflow.common.dto.OrderItemDto;
import com.orderflow.inventoryservice.entity.Product;
import com.orderflow.inventoryservice.entity.Category;
import com.orderflow.inventoryservice.entity.StockReservation;
import com.orderflow.inventoryservice.repository.ProductRepository;
import com.orderflow.inventoryservice.repository.StockReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void reserveStock_SufficientStock_Success() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .stockQuantity(10)
                .price(new BigDecimal("50.00"))
                .category(Category.ELECTRONICS)
                .build();

        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .productName("Test Product")
                .quantity(3)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockReservationRepository.save(any(StockReservation.class)))
                .thenReturn(StockReservation.builder().id(1L).build());

        boolean result = inventoryService.reserveStock(1L, List.of(item));

        assertTrue(result);
        assertEquals(7, product.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void reserveStock_InsufficientStock_Fails() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .stockQuantity(2)
                .price(new BigDecimal("50.00"))
                .category(Category.ELECTRONICS)
                .build();

        OrderItemDto item = OrderItemDto.builder()
                .productId(1L)
                .quantity(5)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = inventoryService.reserveStock(1L, List.of(item));

        assertFalse(result);
    }

    @Test
    void reserveStock_ProductNotFound_Fails() {
        OrderItemDto item = OrderItemDto.builder()
                .productId(999L)
                .quantity(1)
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = inventoryService.reserveStock(1L, List.of(item));

        assertFalse(result);
    }

    @Test
    void releaseStock_Success() {
        Product product = Product.builder()
                .id(1L)
                .stockQuantity(7)
                .build();

        StockReservation reservation = StockReservation.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .quantity(3)
                .status("RESERVED")
                .build();

        when(stockReservationRepository.findByOrderId(1L)).thenReturn(List.of(reservation));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        inventoryService.releaseStock(1L);

        assertEquals(10, product.getStockQuantity());
        assertEquals("RELEASED", reservation.getStatus());
    }
}
