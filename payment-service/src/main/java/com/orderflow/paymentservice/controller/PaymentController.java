package com.orderflow.paymentservice.controller;

import com.orderflow.common.dto.ApiResponse;
import com.orderflow.common.dto.PaymentDto;
import com.orderflow.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getByOrderId(@PathVariable Long orderId) {
        PaymentDto payment = paymentService.getByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getByUserId(@PathVariable Long userId) {
        List<PaymentDto> payments = paymentService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
