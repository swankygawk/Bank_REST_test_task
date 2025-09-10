package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "users"
)
public class UserEntity implements UserDetails {
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

    @Column(
        name = "username",
        unique = true,
        nullable = false
    )
    private String username;

    @Column(
        name = "password",
        nullable = false
    )
    private String password;

    @ManyToMany(
        fetch = FetchType.EAGER
    )
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
