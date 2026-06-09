package com.example.saga.choreography.inventory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "choreo_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    private String productId;
    private int quantity;
}
