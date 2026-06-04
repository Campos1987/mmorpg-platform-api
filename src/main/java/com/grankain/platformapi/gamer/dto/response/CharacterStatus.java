package com.grankain.platformapi.gamer.dto.response;

public record CharacterStatus(
    String accountName,
    int charId,
    String charName,
    int lvl,
    float maxHp,
    float maxMp,
    float maxCp,
    int race,
    int baseClassId,
    int classId,
    long exp,
    int karma,
    int isOnline
) {
    public CharacterStatus(int charId, String charName, int lvl) {
        this(null, charId, charName, lvl, 0.0f, 0.0f, 0.0f, 0, 0, 0, 0, 0, 0);
    }
}
