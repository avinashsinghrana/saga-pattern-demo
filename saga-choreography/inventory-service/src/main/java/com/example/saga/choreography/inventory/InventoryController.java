package com.example.saga.choreography.inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository repository;
    
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        repository.save(new InventoryItem("ITEM-123", 100));
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestBody OrderEventDTO event) {
        var item = repository.findById(event.getProductId());
        if (item.isPresent() && item.get().getQuantity() >= event.getQuantity()) {
            item.get().setQuantity(item.get().getQuantity() - event.getQuantity());
            repository.save(item.get());
            
            // Emit InventoryReservedEvent (simulate via call to Payment)
            try {
                restTemplate.postForEntity("http://localhost:8083/api/payment/process", event, String.class);
            } catch (Exception e) {
                // Payment call failed immediately -> trigger compensation
                release(event);
                restTemplate.postForEntity("http://localhost:8084/api/orders/" + event.getOrderId() + "/cancel", null, String.class);
            }
            return ResponseEntity.ok("RESERVED");
        }
        
        // Out of stock -> tell order to cancel
        restTemplate.postForEntity("http://localhost:8084/api/orders/" + event.getOrderId() + "/cancel", null, String.class);
        return ResponseEntity.badRequest().body("OUT_OF_STOCK");
    }

    @PostMapping("/release")
    public void release(@RequestBody OrderEventDTO event) {
        var item = repository.findById(event.getProductId());
        if (item.isPresent()) {
            item.get().setQuantity(item.get().getQuantity() + event.getQuantity());
            repository.save(item.get());
        }
    }
}
