package com.grankain.platformapi.dashboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "`user`", columnDefinition = "BINARY(16)", nullable = false)
    private UUID id; // Mapeado da coluna 'user'.ID do usuario dono da conta este ide pode estar em
                     // varias contas

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "lastactive")
    private Long lastActive;

    // Se você já tiver um Enum para AccessLevel, pode alterar o tipo de Integer
    // para o seu Enum
    // e adicionar a anotação @Enumerated(EnumType.ORDINAL) ou (EnumType.STRING)
    @Column(name = "accessLevel")
    private Integer accessLevel;

    @Column(name = "lastIP")
    private String lastIp;

    @Column(name = "lastServer")
    private Integer lastServer;

    @Column(name = "pcIp")
    private String pcIp;

    @Column(name = "hop1")
    private String hop1;

    @Column(name = "hop2")
    private String hop2;

    @Column(name = "hop3")
    private String hop3;

    @Column(name = "hop4")
    private String hop4;

    // ==========================================
    // Construtores, Getters e Setters
    // (Caso não esteja usando o Lombok com @Data)
    // ==========================================

    public Account() {
        // JPA standard no-args constructor
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Long getLastActive() {
        return lastActive;
    }

    public void setLastActive(Long lastActive) {
        this.lastActive = lastActive;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public Integer getLastServer() {
        return lastServer;
    }

    public void setLastServer(Integer lastServer) {
        this.lastServer = lastServer;
    }

    public String getPcIp() {
        return pcIp;
    }

    public void setPcIp(String pcIp) {
        this.pcIp = pcIp;
    }

    public String getHop1() {
        return hop1;
    }

    public void setHop1(String hop1) {
        this.hop1 = hop1;
    }

    public String getHop2() {
        return hop2;
    }

    public void setHop2(String hop2) {
        this.hop2 = hop2;
    }

    public String getHop3() {
        return hop3;
    }

    public void setHop3(String hop3) {
        this.hop3 = hop3;
    }

    public String getHop4() {
        return hop4;
    }

    public void setHop4(String hop4) {
        this.hop4 = hop4;
    }
}