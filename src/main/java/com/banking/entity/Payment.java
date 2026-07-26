package com.banking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private BillGroup group;

    @ManyToOne
    @JoinColumn(name = "from_participant_id", nullable = false)
    private Participant from;

    @ManyToOne
    @JoinColumn(name = "to_participant_id", nullable = false)
    private Participant to;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        this.paidAt = LocalDateTime.now();
    }
}