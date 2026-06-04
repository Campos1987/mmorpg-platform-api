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
    @Column(name = "charId", nullable = false)
    private Integer charId;

    @Column(name = "char_name", nullable = false)
    private String charName;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @NotNull
    @Column(name = "level", nullable = false)
    private Integer lvl;

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
    @Column(name = "sex", nullable = false)
    private Integer sex;

    @NotNull
    @Column(name = "race", nullable = false)
    private Integer race;

    @NotNull
    @Column(name = "base_classid", nullable = false)
    private Integer baseClassId;

    @NotNull
    @Column(name = "classid", nullable = false)
    private Integer classId;

    @NotNull
    @Column(name = "exp", nullable = false)
    private long exp;

    @NotNull
    @Column(name = "karma", nullable = false)
    private Integer karma;

    @NotNull
    @Column(name = "online", nullable = false)
    private Integer isOnline;
}
