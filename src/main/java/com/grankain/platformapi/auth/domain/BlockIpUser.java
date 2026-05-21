package com.grankain.platformapi.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "ip_user", nullable = false)
    private String ipUser;

    @Column(name = "count", nullable = false)
    private int count;

    @UpdateTimestamp
    @Column(name = "block_at", nullable = false, updatable = true)
    private Instant blockAt;

    public BlockIpUser() {
    }

    public BlockIpUser(String ipUser, int count, Instant blockAt) {
        this.ipUser = ipUser;
        this.count = count;
        this.blockAt = blockAt;
    }
}
