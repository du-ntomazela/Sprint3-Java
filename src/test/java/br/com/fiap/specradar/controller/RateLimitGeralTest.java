package br.com.fiap.specradar.controller;

import br.com.fiap.specradar.domain.usuario.Perfil;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RateLimitGeralTest extends ApiTestBase {

    @Test
    void sessentaEUmaRequisicoesNoMesmoMinutoDeveRetornar429NaUltima() throws Exception {
        String token = "Bearer " + tokenParaNovoUsuario(Perfil.ANALISTA);

        for (int i = 0; i < 60; i++) {
            mockMvc.perform(get("/atributos").header("Authorization", token))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/atributos").header("Authorization", token))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}
