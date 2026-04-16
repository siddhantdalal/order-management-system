package com.orderflow.notificationservice.controller;

import com.orderflow.common.dto.ApiResponse;
import com.orderflow.notificationservice.entity.Notification;
import com.orderflow.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getByUserId(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getByOrderId(@PathVariable Long orderId) {
        List<Notification> notifications = notificationService.getByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
}
