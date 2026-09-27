package br.com.fiap.specradar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AtributoControllerTest extends ApiTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cadastrarComoAdminDeveRetornar201() throws Exception {
        String corpo = objectMapper.writeValueAsString(new CadastroAtributo("teste_potencia_admin", "Potência de teste", "cv", "Motor"));

        mockMvc.perform(post("/atributos")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codigo").value("teste_potencia_admin"));
    }

    @Test
    void cadastrarComoAnalistaDeveRetornar403() throws Exception {
        String corpo = objectMapper.writeValueAsString(new CadastroAtributo("teste_negado", "Negado", "cv", "Motor"));

        mockMvc.perform(post("/atributos")
                        .header("Authorization", "Bearer " + tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void cadastrarSemTokenDeveRetornar401() throws Exception {
        String corpo = objectMapper.writeValueAsString(new CadastroAtributo("teste_sem_token", "Sem token", "cv", "Motor"));

        mockMvc.perform(post("/atributos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void cadastrarComCamposInvalidosDeveRetornar400() throws Exception {
        String corpo = objectMapper.writeValueAsString(new CadastroAtributo("", "", null, ""));

        mockMvc.perform(post("/atributos")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    void listarComoAnalistaDeveRetornar200() throws Exception {
        mockMvc.perform(get("/atributos")
                        .header("Authorization", "Bearer " + tokenAnalista()))
                .andExpect(status().isOk());
    }

    private record CadastroAtributo(String codigo, String nome, String unidade, String categoria) {
    }
}
