package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "cards"
)
public class Card {
    @Id
    @GeneratedValue(
        strategy = GenerationType.UUID
    )
    @Column(
        name = "id",
        nullable = false,
        updatable = false
    )
    private UUID id;

    @Version
    @Column(
        name = "version",
        nullable = false
    )
    private Long version;

    @Column(
        name = "number",
        unique = true,
        nullable = false
    )
    private String number;

    @Column(
        name = "expiry_date",
        nullable = false
    )
    // MM/YY format
    private String expiryDate;

    @Column
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(
        name = "balance",
        nullable = false,
        precision = 19,
        scale = 4
    )
    private BigDecimal balance;

    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User holder;
}
