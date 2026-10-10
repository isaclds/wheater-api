package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.alerta.AlertaRequestDTO;
import ifsc.edu.lll.dto.alerta.AlertaResponseDTO;
import ifsc.edu.lll.model.Alerta;
import ifsc.edu.lll.repository.AlertaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaService — cadastro de alertas")
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    @DisplayName("Deve salvar alerta e retornar DTO com dados corretos")
    void cadastrarDeveRetornarDtoComDadosCorretos() {
        AlertaRequestDTO dto = new AlertaRequestDTO(42L, "Brasil", "Florianópolis", 35.0, 5.0, 20.0);
        Alerta alertaSalvo = new Alerta(42L, "Brasil", "Florianópolis", 35.0, 5.0, 20.0);
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaSalvo);

        AlertaResponseDTO resultado = alertaService.cadastrar(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.canalId()).isEqualTo(42L);
        assertThat(resultado.pais()).isEqualTo("Brasil");
        assertThat(resultado.cidade()).isEqualTo("Florianópolis");
        assertThat(resultado.temperaturaMaxima()).isEqualTo(35.0);
        assertThat(resultado.ativo()).isTrue();
    }

    @Test
    @DisplayName("Deve chamar repository.save exatamente uma vez")
    void cadastrarDeveChmarSaveUmaVez() {
        AlertaRequestDTO dto = new AlertaRequestDTO(1L, "BR", "Curitiba", null, null, null);
        Alerta alertaSalvo = new Alerta(1L, "BR", "Curitiba", null, null, null);
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaSalvo);

        alertaService.cadastrar(dto);

        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Alerta criado deve ser enviado ao repository com ativo=true")
    void alertaCriadoDeveEstarAtivo() {
        AlertaRequestDTO dto = new AlertaRequestDTO(1L, "BR", "Curitiba", 40.0, null, null);
        Alerta alertaSalvo = new Alerta(1L, "BR", "Curitiba", 40.0, null, null);
        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaSalvo);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        alertaService.cadastrar(dto);

        verify(alertaRepository).save(captor.capture());
        assertThat(captor.getValue().isAtivo()).isTrue();
    }
}
