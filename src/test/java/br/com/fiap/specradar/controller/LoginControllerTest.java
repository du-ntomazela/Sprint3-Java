package br.com.fiap.specradar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LoginControllerTest extends ApiTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void loginComCredenciaisValidasDeveRetornar200ComToken() throws Exception {
        String corpo = objectMapper.writeValueAsString(new Login(LOGIN_ADMIN, SENHA_ADMIN));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenJWT").isNotEmpty());
    }

    @Test
    @Order(2)
    void loginComSenhaInvalidaDeveRetornar401() throws Exception {
        String corpo = objectMapper.writeValueAsString(new Login(LOGIN_ADMIN, "senha-errada"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @Order(3)
    void loginComCamposEmBrancoDeveRetornar400() throws Exception {
        String corpo = objectMapper.writeValueAsString(new Login("", ""));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void seisTentativasDeLoginNoMesmoMinutoDeveRetornar429NaSexta() throws Exception {
        String corpo = objectMapper.writeValueAsString(new Login(LOGIN_ADMIN, "senha-errada"));
        String ipDeTeste = "203.0.113.77";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/login")
                            .header("X-Forwarded-For", ipDeTeste)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpo))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/login")
                        .header("X-Forwarded-For", ipDeTeste)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    private record Login(String login, String senha) {
    }
}
