package com.app.fitness.controller.loadTesting;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/loadbalancer-demo")
public class LoadBalancerDemoController {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String serviceId;

    public LoadBalancerDemoController(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/call-self")
    public String callSelf() {
        String url = "http://" + serviceId + "/loadbalancer-demo/instance-info";
        return restTemplate.getForObject(url, String.class);
    }

    @GetMapping("/instance-info")
    public String getInstanceInfo() {
        List<String> instances = discoveryClient.getServices();
        String instanceId = "Unknown";
        if (instances != null && !instances.isEmpty()) {
            instanceId = discoveryClient.getInstances(serviceId).stream()
                    .findFirst()
                    .map(si -> si.getHost() + ":" + si.getPort())
                    .orElse("Unknown");
        }
        return "Hello from workout-service instance: " + instanceId;
    }
}
