package com.costrip.costrip_backend.entity;

import com.costrip.costrip_backend.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계: User 1 ──── N CardHistory (다 쪽 외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime usedAt;

    @Column(nullable = false, length = 200)
    private String merchantName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    // 연관관계: CardHistory 0..1 ──── 1 Expense (선택적 연결, 중복 불가)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_expense_id", unique = true)
    private Expense linkedExpense;
}
