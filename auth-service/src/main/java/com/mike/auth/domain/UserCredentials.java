package com.mike.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class UserCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected UserCredentials() {}

    public UserCredentials(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}

