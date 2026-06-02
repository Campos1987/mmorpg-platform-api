package com.grankain.platformapi.gamer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Table(name = "characters")
public class Character {

    @Id
    @Setter
    @NotNull(message = "Character name is required")
    @Column(name = "char_name", nullable = false)
    private String charName;

    @Setter
    @NotNull(message = "Account name is required")
    @Column(name = "account_name", nullable = false)
    private String accountName;
}
