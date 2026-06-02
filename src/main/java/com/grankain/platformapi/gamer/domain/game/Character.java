package com.grankain.platformapi.gamer.domain.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Entity
@Getter
@Table(name = "characters")
public class Character {

    @Id
    @Column(name = "char_name", nullable = false)
    private String charName;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @NotNull
    @Column(name = "level", nullable = false)
    private int lvl;

    @NotNull
    @Column(name = "maxhp", nullable = false)
    private float maxHp;

    @NotNull
    @Column(name = "maxmp", nullable = false)
    private float maxMp;

    @NotNull
    @Column(name = "maxcp", nullable = false)
    private float maxCp;

    @NotNull
    @Column(name = "race", nullable = false)
    private int race;

    @NotNull
    @Column(name = "base_classid", nullable = false)
    private int baseClassId;

    @NotNull
    @Column(name = "classid", nullable = false)
    private int classId;

    @NotNull
    @Column(name = "exp", nullable = false)
    private long exp;

    @NotNull
    @Column(name = "karma", nullable = false)
    private int karma;
}
