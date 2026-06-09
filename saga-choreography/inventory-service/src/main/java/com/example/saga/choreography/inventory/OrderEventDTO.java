package com.example.saga.choreography.inventory;
import lombok.Data;
@Data
public class OrderEventDTO {
    private Long orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private double price;
}
