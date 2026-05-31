package com.grankain.platformapi.dashboard.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "accounts")
@Getter
public class Accounts {

    @Id
    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_time", nullable = false)
    private String createdTime;

    @Column(name = "lastactive")
    private String lastactive;
}
