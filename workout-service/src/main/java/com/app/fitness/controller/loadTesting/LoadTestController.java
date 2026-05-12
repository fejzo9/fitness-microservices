package com.app.fitness.controller.loadTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/load-test")
public class LoadTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadTestController.class);
    private final AtomicLong requestCounter = new AtomicLong(0);
    
    @Value("${eureka.instance.instance-id:unknown}")
    private String instanceId;
    
    @Value("${server.port:8081}")
    private String serverPort;
    
    @GetMapping
    public Map<String, Object> handleRequest() {
        long requestId = requestCounter.incrementAndGet();
        
        // Simulate some processing time
        try {
            Thread.sleep(10 + (int)(Math.random() * 20)); // 10-30ms processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("instanceId", instanceId);
        response.put("serverPort", serverPort);
        response.put("requestId", requestId);
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Load test response from workout service");
        
        logger.info("Request #{} handled by instance {} on port {}", 
                   requestId, instanceId, serverPort);
        
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("instanceId", instanceId);
        response.put("serverPort", serverPort);
        response.put("totalRequests", requestCounter.get());
        return response;
    }
}
