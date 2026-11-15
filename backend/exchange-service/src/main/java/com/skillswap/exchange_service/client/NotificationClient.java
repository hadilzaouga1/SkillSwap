package com.skillswap.exchange_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8089}")
public interface NotificationClient {
    
    @PostMapping("/internal/notifications/reminder")
    void envoyerRappel(@RequestBody ReminderRequest request);
}