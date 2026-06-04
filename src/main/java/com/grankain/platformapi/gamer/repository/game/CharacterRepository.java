package com.grankain.platformapi.gamer.repository.game;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.grankain.platformapi.gamer.domain.game.Character;

public interface CharacterRepository extends JpaRepository<Character, Integer> {

    @Query("SELECT c FROM Character c WHERE c.accountName = :accountName")
    List<Character> findAllByAccountName(String accountName);

    Character findByCharId(int charId);

    Character findByCharName(String charName);
}
