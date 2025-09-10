package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "roles"
)
public class Role {
    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    @Column(
        name = "id",
        nullable = false,
        updatable = false
    )
    private Integer id;

    @Column(
        name = "name",
        unique = true,
        nullable = false
    )
    private String name;
}
