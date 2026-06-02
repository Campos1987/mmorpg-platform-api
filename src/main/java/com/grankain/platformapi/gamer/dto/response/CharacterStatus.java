package com.grankain.platformapi.gamer.dto.response;

public record CharacterStatus(
    String charName,
    int lvl,
    float maxHp,
    float maxMp,
    float maxCp,
    int race,
    int baseClassId,
    int classId,
    long exp,
    int karma    
) {
    
}
