package br.com.fiap.specradar.controller;

import br.com.fiap.specradar.domain.usuario.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UsuarioControllerTest extends ApiTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void meuUsuarioDeveRetornarDadosDoUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/usuarios/me").header("Authorization", "Bearer " + tokenAnalista()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(LOGIN_ANALISTA))
                .andExpect(jsonPath("$.perfil").value("ANALISTA"));
    }

    @Test
    void alterarPerfilComoAdminDeveRetornar200() throws Exception {
        Usuario analista = buscarUsuario(LOGIN_ANALISTA);
        String corpo = objectMapper.writeValueAsString(new AtualizacaoPerfil("ADMIN"));

        mockMvc.perform(patch("/usuarios/" + analista.getId() + "/perfil")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void alterarPerfilComoAnalistaDeveRetornar403() throws Exception {
        Usuario admin = buscarUsuario(LOGIN_ADMIN);
        String corpo = objectMapper.writeValueAsString(new AtualizacaoPerfil("ANALISTA"));

        mockMvc.perform(patch("/usuarios/" + admin.getId() + "/perfil")
                        .header("Authorization", "Bearer " + tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }

    private record AtualizacaoPerfil(String perfil) {
    }
}
