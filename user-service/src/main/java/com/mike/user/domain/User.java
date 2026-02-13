package com.mike.user.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private boolean blocked;

    protected User() {}

    public User(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = UserStatus.ACTIVE;
        this.blocked = false;
    }

    public void update(String username) {
        this.username = username;
    }

    public void userBlock() {
        this.blocked = true;
        this.status = UserStatus.BLOCKED;
    }
}
