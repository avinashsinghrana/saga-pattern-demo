package com.example.saga.choreography.payment;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "choreo_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    private String customerId;
    private double balance;
}
