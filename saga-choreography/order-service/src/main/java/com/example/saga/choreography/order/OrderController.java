package com.example.saga.choreography.order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository repository;
    
    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderCreateRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());
        order.setStatus("PENDING");
        order = repository.save(order);

        // Emit OrderCreatedEvent (simulated via REST call to Inventory)
        OrderEventDTO event = new OrderEventDTO();
        event.setOrderId(order.getId());
        event.setCustomerId(order.getCustomerId());
        event.setProductId(order.getProductId());
        event.setQuantity(order.getQuantity());
        event.setPrice(order.getPrice());
        
        try {
            restTemplate.postForEntity("http://localhost:8082/api/inventory/reserve", event, String.class);
        } catch (Exception e) {
            order.setStatus("CANCELLED");
            repository.save(order);
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/approve")
    public void approveOrder(@PathVariable Long orderId) {
        repository.findById(orderId).ifPresent(order -> {
            order.setStatus("APPROVED");
            repository.save(order);
        });
    }

    @PostMapping("/{orderId}/cancel")
    public void cancelOrder(@PathVariable Long orderId) {
        repository.findById(orderId).ifPresent(order -> {
            order.setStatus("CANCELLED");
            repository.save(order);
        });
    }
}
