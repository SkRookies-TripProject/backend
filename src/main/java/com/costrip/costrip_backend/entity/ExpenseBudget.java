package com.costrip.costrip_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_budgets")
@Getter @NoArgsConstructor
@Setter
@AllArgsConstructor
@Builder
public class ExpenseBudget {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private String category;  // "식비", "쇼핑" 등

    private Long amount;

    public ExpenseBudget(Trip trip, String category, Long amount) {
        this.trip     = trip;
        this.category = category;
        this.amount   = amount;
    }
}