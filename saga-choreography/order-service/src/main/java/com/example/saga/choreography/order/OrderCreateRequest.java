package com.example.saga.choreography.order;
import lombok.Data;
@Data
public class OrderCreateRequest {
    private String customerId;
    private String productId;
    private int quantity;
    private double price;
}
