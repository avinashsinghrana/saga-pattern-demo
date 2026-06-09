package com.example.saga.choreography.payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private AccountRepository repository;
    
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        repository.save(new Account("CUST-1", 500.0));
    }

    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestBody OrderEventDTO event) {
        var account = repository.findById(event.getCustomerId());
        double amount = event.getPrice() * event.getQuantity();
        
        if (account.isPresent() && account.get().getBalance() >= amount) {
            account.get().setBalance(account.get().getBalance() - amount);
            repository.save(account.get());
            
            // Success -> Tell Order to Approve
            restTemplate.postForEntity("http://localhost:8084/api/orders/" + event.getOrderId() + "/approve", null, String.class);
            return ResponseEntity.ok("PROCESSED");
        }
        
        // Failure -> Emit PaymentFailedEvent -> Compensate Inventory and Order
        restTemplate.postForEntity("http://localhost:8082/api/inventory/release", event, String.class);
        restTemplate.postForEntity("http://localhost:8084/api/orders/" + event.getOrderId() + "/cancel", null, String.class);
        
        return ResponseEntity.badRequest().body("INSUFFICIENT_FUNDS");
    }
}
