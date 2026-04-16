package com.orderflow.inventoryservice.service;

import com.orderflow.common.dto.OrderItemDto;
import com.orderflow.inventoryservice.entity.Product;
import com.orderflow.inventoryservice.entity.StockReservation;
import com.orderflow.inventoryservice.repository.ProductRepository;
import com.orderflow.inventoryservice.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;

    @Override
    @Transactional
    public boolean reserveStock(Long orderId, List<OrderItemDto> items) {
        List<StockReservation> reservations = new ArrayList<>();

        for (OrderItemDto item : items) {
            Optional<Product> productOpt = productRepository.findById(item.getProductId());
            if (productOpt.isEmpty()) {
                log.warn("Product not found: id={}", item.getProductId());
                rollbackReservations(reservations);
                return false;
            }

            Product product = productOpt.get();
            if (product.getStockQuantity() < item.getQuantity()) {
                log.warn("Insufficient stock: productId={}, requested={}, available={}",
                        item.getProductId(), item.getQuantity(), product.getStockQuantity());
                rollbackReservations(reservations);
                return false;
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);

            StockReservation reservation = StockReservation.builder()
                    .orderId(orderId)
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .status("RESERVED")
                    .build();
            reservations.add(stockReservationRepository.save(reservation));
        }

        log.info("Stock reserved for order: orderId={}, items={}", orderId, items.size());
        return true;
    }

    @Override
    @Transactional
    public void releaseStock(Long orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            if ("RESERVED".equals(reservation.getStatus())) {
                productRepository.findById(reservation.getProductId()).ifPresent(product -> {
                    product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
                    productRepository.save(product);
                });
                reservation.setStatus("RELEASED");
                stockReservationRepository.save(reservation);
            }
        }

        log.info("Stock released for order: orderId={}", orderId);
    }

    private void rollbackReservations(List<StockReservation> reservations) {
        for (StockReservation reservation : reservations) {
            productRepository.findById(reservation.getProductId()).ifPresent(product -> {
                product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
                productRepository.save(product);
            });
            reservation.setStatus("RELEASED");
            stockReservationRepository.save(reservation);
        }
    }
}
