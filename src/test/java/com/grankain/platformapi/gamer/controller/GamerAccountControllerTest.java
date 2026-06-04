package com.grankain.platformapi.gamer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grankain.platformapi.gamer.dto.request.CreateAccountRequest;
import com.grankain.platformapi.gamer.dto.response.AccountCharactersResponse;
import com.grankain.platformapi.gamer.dto.response.CharacterStatus;
import com.grankain.platformapi.gamer.service.CharacterService;
import com.grankain.platformapi.gamer.service.GamerAccountService;

@WebMvcTest(GamerAccountController.class)
class GamerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private GamerAccountService gamerAccountService;

    @MockitoBean
    private CharacterService characterService;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
    }

    @Test
    void getGameAccount_WhenAuthenticated_ShouldReturnAccountList() throws Exception {
        // Arrange
        AccountCharactersResponse response = new AccountCharactersResponse(
                UUID.randomUUID(), "loginL2", 0, List.of());
        when(gamerAccountService.findGameAccount(ownerId)).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(post("/gamer/account")
                .with(jwt().jwt(j -> j.subject(ownerId.toString())))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(response))));

        verify(gamerAccountService, times(1)).findGameAccount(ownerId);
    }

    @Test
    void findCharacter_WhenAuthenticated_ShouldReturnCharacterStatus() throws Exception {
        // Arrange
        CharacterStatus character = new CharacterStatus("loginL2", 100002, "Hero", 80);
        when(characterService.findCharacter(eq(ownerId), eq("100002"))).thenReturn(character);

        // Act & Assert
        mockMvc.perform(get("/gamer/findCharacters/{charId}", "100002")
                .with(jwt().jwt(j -> j.subject(ownerId.toString()))))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(character)));

        verify(characterService, times(1)).findCharacter(ownerId, "100002");
    }

    @Test
    void createGameAccount_WhenValidRequest_ShouldReturnTrue() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("validlogin", "Secure@12");
        when(gamerAccountService.createGameAccount(eq(ownerId), any(CreateAccountRequest.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/gamer/create")
                .with(jwt().jwt(j -> j.subject(ownerId.toString())))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(gamerAccountService, times(1)).createGameAccount(eq(ownerId), any(CreateAccountRequest.class));
    }

    @Test
    void blockAccount_WhenAuthenticated_ShouldReturnTrue() throws Exception {
        // Arrange
        String accountIdBlock = UUID.randomUUID().toString();
        when(gamerAccountService.blockAccount(ownerId, accountIdBlock)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/gamer/block/{accountIdBlock}", accountIdBlock)
                .with(jwt().jwt(j -> j.subject(ownerId.toString()))))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(gamerAccountService, times(1)).blockAccount(ownerId, accountIdBlock);
    }
}
