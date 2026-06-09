package com.example.saga.choreography.order;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saga_choreo_orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerId;
    private String productId;
    private int quantity;
    private double price;
    private String status;
}
