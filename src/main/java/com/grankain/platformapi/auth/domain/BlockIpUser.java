package com.grankain.platformapi.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "block_ip_user")
@Getter
@Setter
public class BlockIpUser {

    @Id
    @GeneratedValue
    Long id;
    @Column(name = "ip_user", nullable = false, unique = true)
    private String ipUser;

    @Column(name = "count", nullable = false)
    private int count;

    @Column(name = "block_at")
    private Instant blockAt;

    public BlockIpUser() {
    }

    public BlockIpUser(String ipUser, int count, Instant blockAt) {
        this.ipUser = ipUser;
        this.count = count;
        this.blockAt = blockAt;
    }
}
