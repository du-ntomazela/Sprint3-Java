package br.com.fiap.specradar.controller;

import br.com.fiap.specradar.domain.atributo.Atributo;
import br.com.fiap.specradar.domain.atributo.AtributoRepository;
import br.com.fiap.specradar.domain.usuario.Usuario;
import br.com.fiap.specradar.domain.veiculo.EspecificacaoVeiculo;
import br.com.fiap.specradar.domain.veiculo.Veiculo;
import br.com.fiap.specradar.domain.veiculo.VeiculoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class PesquisaControllerTest extends ApiTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private AtributoRepository atributoRepository;

    @Test
    void pesquisarComAtributosInvalidosDeveRetornar400() throws Exception {
        String corpo = objectMapper.writeValueAsString(new CadastroPesquisa("Ford", "Ranger", "Raptor", List.of()));

        mockMvc.perform(post("/pesquisas")
                        .header("Authorization", "Bearer " + tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pesquisarSemTokenDeveRetornar401() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                new CadastroPesquisa("Ford", "Ranger", "Raptor", List.of("potencia")));

        mockMvc.perform(post("/pesquisas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pesquisarComTokenExpiradoDeveRetornar401() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                new CadastroPesquisa("Ford", "Ranger", "Raptor", List.of("potencia")));

        mockMvc.perform(post("/pesquisas")
                        .header("Authorization", "Bearer " + tokenExpirado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pesquisarVeiculoConhecidoDeveRetornarFichaNoFormatoFixo() throws Exception {
        Atributo potencia = atributoRepository.findByCodigoAndAtivoTrue("potencia").orElseThrow();
        Veiculo veiculo = new Veiculo(null, "Ford", "Ranger", "Raptor Teste", 2024, true, new ArrayList<>());
        veiculoRepository.save(veiculo);
        veiculo.getEspecificacoes().add(new EspecificacaoVeiculo(veiculo, potencia, "292 cv", "Ficha técnica Ford"));
        veiculoRepository.save(veiculo);

        String corpo = objectMapper.writeValueAsString(new CadastroPesquisa(
                "Ford", "Ranger", "Raptor Teste", List.of("potencia", "atributo_inexistente_xyz")));

        mockMvc.perform(post("/pesquisas")
                        .header("Authorization", "Bearer " + tokenAnalista())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.veiculo.marca").value("Ford"))
                .andExpect(jsonPath("$.veiculo.modelo").value("Ranger"))
                .andExpect(jsonPath("$.veiculo.versao").value("Raptor Teste"))
                .andExpect(jsonPath("$.geradoEm").exists())
                .andExpect(jsonPath("$.especificacoes[0].atributoSolicitado").value("potencia"))
                .andExpect(jsonPath("$.especificacoes[0].atributoCanonico").value("Potência"))
                .andExpect(jsonPath("$.especificacoes[0].valor").value("292 cv"))
                .andExpect(jsonPath("$.especificacoes[0].status").value("ENCONTRADO"))
                .andExpect(jsonPath("$.especificacoes[1].atributoCanonico").value("Não disponível"))
                .andExpect(jsonPath("$.especificacoes[1].status").value("NAO_DISPONIVEL"));
    }

    @Test
    void pesquisarRangerRaptorSemeadaDeveRetornarFormatoFixoComNaoDisponivel() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                new CadastroPesquisa("Ford", "Ranger", "Raptor", List.of("potencia", "torque")));

        mockMvc.perform(post("/pesquisas")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.veiculo.marca").value("Ford"))
                .andExpect(jsonPath("$.veiculo.modelo").value("Ranger"))
                .andExpect(jsonPath("$.veiculo.versao").value("Raptor"))
                .andExpect(jsonPath("$.especificacoes[0].atributoSolicitado").value("potencia"))
                .andExpect(jsonPath("$.especificacoes[0].atributoCanonico").value("Potência"))
                .andExpect(jsonPath("$.especificacoes[0].unidade").value("cv"))
                .andExpect(jsonPath("$.especificacoes[0].valor").value("Não disponível"))
                .andExpect(jsonPath("$.especificacoes[0].status").value("NAO_DISPONIVEL"));
    }

    @Test
    void listarPesquisasRetornaApenasDoUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/pesquisas")
                        .header("Authorization", "Bearer " + tokenAnalista()))
                .andExpect(status().isOk());
    }

    @Test
    void detalharPesquisaDeOutroUsuarioDeveRetornar404() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                new CadastroPesquisa("Ford", "Ka", "SE", List.of("potencia")));

        String resposta = mockMvc.perform(post("/pesquisas")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        Long id = Long.valueOf(resposta.substring(resposta.lastIndexOf('/') + 1));

        mockMvc.perform(get("/pesquisas/" + id)
                        .header("Authorization", "Bearer " + tokenAnalista()))
                .andExpect(status().isNotFound());
    }

    private record CadastroPesquisa(String marca, String modelo, String versao, List<String> atributos) {
    }
}
