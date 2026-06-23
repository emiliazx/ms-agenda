package com.costuras.agenda.service;

import com.costuras.agenda.dto.*;
import com.costuras.agenda.model.BloqueoManual;
import com.costuras.agenda.model.DiaFestivo;
import com.costuras.agenda.model.HorarioDisponible;
import com.costuras.agenda.repository.BloqueoManualRepository;
import com.costuras.agenda.repository.DiaFestivoRepository;
import com.costuras.agenda.repository.HorarioDisponibleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private HorarioDisponibleRepository horarioRepo;

    @Mock
    private DiaFestivoRepository festivoRepo;

    @Mock
    private BloqueoManualRepository bloqueoRepo;

    @Mock
    private DisponibilidadClient disponibilidadClient;

    @InjectMocks
    private AgendaService agendaService;

    // ---------- Horarios ----------

    @Test
    void listarHorarios_debeRetornarHorariosActivos() {
        HorarioDisponible h = HorarioDisponible.builder()
                .id(1).diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(18, 0))
                .duracionSlotMinutos(30).activo(true)
                .build();
        when(horarioRepo.findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc()).thenReturn(List.of(h));

        List<HorarioResponse> resultado = agendaService.listarHorarios();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void crearHorario_horaFinPosterior_debeCrear() {
        HorarioRequest req = new HorarioRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), 30);

        when(horarioRepo.save(any(HorarioDisponible.class))).thenAnswer(inv -> {
            HorarioDisponible h = inv.getArgument(0);
            h.setId(1);
            return h;
        });

        HorarioResponse resultado = agendaService.crearHorario(req);

        assertThat(resultado.getId()).isEqualTo(1);
        assertThat(resultado.getActivo()).isTrue();
    }

    @Test
    void crearHorario_horaFinAntesQueInicio_debeLanzarExcepcion() {
        HorarioRequest req = new HorarioRequest(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(9, 0), 30);

        assertThatThrownBy(() -> agendaService.crearHorario(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");

        verify(horarioRepo, never()).save(any());
    }

    @Test
    void crearHorario_horaFinIgualAInicio_debeLanzarExcepcion() {
        HorarioRequest req = new HorarioRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 0), 30);

        assertThatThrownBy(() -> agendaService.crearHorario(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarHorario_existente_debeActualizar() {
        HorarioDisponible existente = HorarioDisponible.builder()
                .id(1).diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(18, 0))
                .duracionSlotMinutos(30).activo(true)
                .build();
        HorarioRequest req = new HorarioRequest(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0), 60);

        when(horarioRepo.findById(1)).thenReturn(Optional.of(existente));
        when(horarioRepo.save(any(HorarioDisponible.class))).thenAnswer(inv -> inv.getArgument(0));

        HorarioResponse resultado = agendaService.actualizarHorario(1, req);

        assertThat(resultado.getDiaSemana()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(resultado.getDuracionSlotMinutos()).isEqualTo(60);
    }

    @Test
    void actualizarHorario_inexistente_debeLanzarExcepcion() {
        when(horarioRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.actualizarHorario(99, new HorarioRequest()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Horario no encontrado");
    }

    @Test
    void eliminarHorario_existente_debeDesactivar() {
        HorarioDisponible h = HorarioDisponible.builder().id(1).activo(true).build();
        when(horarioRepo.findById(1)).thenReturn(Optional.of(h));

        Map<String, String> resultado = agendaService.eliminarHorario(1);

        assertThat(h.getActivo()).isFalse();
        assertThat(resultado.get("mensaje")).isEqualTo("Horario desactivado correctamente");
        verify(horarioRepo).save(h);
    }

    @Test
    void eliminarHorario_inexistente_debeLanzarExcepcion() {
        when(horarioRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.eliminarHorario(99))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ---------- Festivos ----------

    @Test
    void listarFestivos_debeRetornarOrdenados() {
        DiaFestivo f = DiaFestivo.builder().id(1).fecha(LocalDate.of(2026, 12, 25)).descripcion("Navidad").build();
        when(festivoRepo.findAllByOrderByFechaAsc()).thenReturn(List.of(f));

        List<DiaFestivoResponse> resultado = agendaService.listarFestivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDescripcion()).isEqualTo("Navidad");
    }

    @Test
    void crearFestivo_fechaDisponible_debeCrear() {
        DiaFestivoRequest req = new DiaFestivoRequest(LocalDate.of(2026, 12, 25), "Navidad");

        when(festivoRepo.existsByFecha(req.getFecha())).thenReturn(false);
        when(festivoRepo.save(any(DiaFestivo.class))).thenAnswer(inv -> {
            DiaFestivo f = inv.getArgument(0);
            f.setId(1);
            return f;
        });

        DiaFestivoResponse resultado = agendaService.crearFestivo(req);

        assertThat(resultado.getId()).isEqualTo(1);
        assertThat(resultado.getDescripcion()).isEqualTo("Navidad");
    }

    @Test
    void crearFestivo_fechaDuplicada_debeLanzarExcepcion() {
        DiaFestivoRequest req = new DiaFestivoRequest(LocalDate.of(2026, 12, 25), "Navidad");
        when(festivoRepo.existsByFecha(req.getFecha())).thenReturn(true);

        assertThatThrownBy(() -> agendaService.crearFestivo(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un día festivo");

        verify(festivoRepo, never()).save(any());
    }

    @Test
    void eliminarFestivo_existente_debeEliminar() {
        DiaFestivo f = DiaFestivo.builder().id(1).fecha(LocalDate.now()).descripcion("X").build();
        when(festivoRepo.findById(1)).thenReturn(Optional.of(f));

        Map<String, String> resultado = agendaService.eliminarFestivo(1);

        assertThat(resultado.get("mensaje")).isEqualTo("Día festivo eliminado correctamente");
        verify(festivoRepo).deleteById(1);
    }

    @Test
    void eliminarFestivo_inexistente_debeLanzarExcepcion() {
        when(festivoRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.eliminarFestivo(99))
                .isInstanceOf(NoSuchElementException.class);

        verify(festivoRepo, never()).deleteById(any());
    }

    // ---------- Bloqueos ----------

    @Test
    void listarBloqueos_conRango_debeFiltrarPorFechas() {
        BloqueoManual b = BloqueoManual.builder().id(1).fecha(LocalDate.of(2026, 6, 1)).motivo("Vacaciones").build();
        LocalDate desde = LocalDate.of(2026, 6, 1);
        LocalDate hasta = LocalDate.of(2026, 6, 30);

        when(bloqueoRepo.findByFechaBetweenOrderByFechaAscHoraInicioAsc(desde, hasta)).thenReturn(List.of(b));

        List<BloqueoResponse> resultado = agendaService.listarBloqueos(desde, hasta);

        assertThat(resultado).hasSize(1);
        verify(bloqueoRepo, never()).findAll();
    }

    @Test
    void listarBloqueos_sinRango_debeRetornarTodos() {
        BloqueoManual b = BloqueoManual.builder().id(1).fecha(LocalDate.now()).motivo("X").build();
        when(bloqueoRepo.findAll()).thenReturn(List.of(b));

        List<BloqueoResponse> resultado = agendaService.listarBloqueos(null, null);

        assertThat(resultado).hasSize(1);
        verify(bloqueoRepo, never()).findByFechaBetweenOrderByFechaAscHoraInicioAsc(any(), any());
    }

    @Test
    void crearBloqueo_debeGuardarYRetornar() {
        BloqueoRequest req = new BloqueoRequest(LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(12, 0), "Mantención");

        when(bloqueoRepo.save(any(BloqueoManual.class))).thenAnswer(inv -> {
            BloqueoManual b = inv.getArgument(0);
            b.setId(1);
            return b;
        });

        BloqueoResponse resultado = agendaService.crearBloqueo(req);

        assertThat(resultado.getId()).isEqualTo(1);
        assertThat(resultado.getMotivo()).isEqualTo("Mantención");
    }

    @Test
    void eliminarBloqueo_existente_debeEliminar() {
        BloqueoManual b = BloqueoManual.builder().id(1).fecha(LocalDate.now()).build();
        when(bloqueoRepo.findById(1)).thenReturn(Optional.of(b));

        Map<String, String> resultado = agendaService.eliminarBloqueo(1);

        assertThat(resultado.get("mensaje")).isEqualTo("Bloqueo eliminado correctamente");
        verify(bloqueoRepo).deleteById(1);
    }

    @Test
    void eliminarBloqueo_inexistente_debeLanzarExcepcion() {
        when(bloqueoRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.eliminarBloqueo(99))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ---------- obtenerDisponibilidad ----------

    @Test
    void obtenerDisponibilidad_diaFestivo_debeRetornarNoDisponible() {
        LocalDate fecha = LocalDate.of(2026, 12, 25);
        when(festivoRepo.existsByFecha(fecha)).thenReturn(true);

        DisponibilidadDiaResponse resultado = agendaService.obtenerDisponibilidad(fecha);

        assertThat(resultado.isDisponible()).isFalse();
        assertThat(resultado.getMotivo()).isEqualTo("Día festivo");
        assertThat(resultado.getSlots()).isEmpty();
        verify(horarioRepo, never()).findByDiaSemanaAndActivoTrue(any());
    }

    @Test
    void obtenerDisponibilidad_sinHorarioParaEseDia_debeRetornarNoDisponible() {
        LocalDate fecha = LocalDate.of(2026, 6, 1); // lunes
        when(festivoRepo.existsByFecha(fecha)).thenReturn(false);
        when(horarioRepo.findByDiaSemanaAndActivoTrue(fecha.getDayOfWeek())).thenReturn(List.of());

        DisponibilidadDiaResponse resultado = agendaService.obtenerDisponibilidad(fecha);

        assertThat(resultado.isDisponible()).isFalse();
        assertThat(resultado.getMotivo()).isEqualTo("Sin horario de atención para este día");
    }

    @Test
    void obtenerDisponibilidad_bloqueoDiaCompleto_debeRetornarNoDisponibleConMotivo() {
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        HorarioDisponible horario = HorarioDisponible.builder()
                .diaSemana(fecha.getDayOfWeek())
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(12, 0))
                .duracionSlotMinutos(30).activo(true)
                .build();
        BloqueoManual bloqueoDia = BloqueoManual.builder()
                .fecha(fecha).horaInicio(null).horaFin(null).motivo("Cierre por feriado interno")
                .build();

        when(festivoRepo.existsByFecha(fecha)).thenReturn(false);
        when(horarioRepo.findByDiaSemanaAndActivoTrue(fecha.getDayOfWeek())).thenReturn(List.of(horario));
        when(bloqueoRepo.findByFechaOrderByHoraInicioAsc(fecha)).thenReturn(List.of(bloqueoDia));

        DisponibilidadDiaResponse resultado = agendaService.obtenerDisponibilidad(fecha);

        assertThat(resultado.isDisponible()).isFalse();
        assertThat(resultado.getMotivo()).isEqualTo("Cierre por feriado interno");
        verify(disponibilidadClient, never()).obtenerHorasReservadas(any());
    }

    @Test
    void obtenerDisponibilidad_conSlotsLibresYOcupados_debeCalcularCorrectamente() {
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        HorarioDisponible horario = HorarioDisponible.builder()
                .diaSemana(fecha.getDayOfWeek())
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(10, 0))
                .duracionSlotMinutos(30).activo(true)
                .build();

        when(festivoRepo.existsByFecha(fecha)).thenReturn(false);
        when(horarioRepo.findByDiaSemanaAndActivoTrue(fecha.getDayOfWeek())).thenReturn(List.of(horario));
        when(bloqueoRepo.findByFechaOrderByHoraInicioAsc(fecha)).thenReturn(List.of());
        when(bloqueoRepo.existsBloqueoEnHorario(eq(fecha), any(), any())).thenReturn(false);
        when(disponibilidadClient.obtenerHorasReservadas(fecha)).thenReturn(Set.of(LocalTime.of(9, 30)));

        DisponibilidadDiaResponse resultado = agendaService.obtenerDisponibilidad(fecha);

        assertThat(resultado.getSlots()).hasSize(2); // 09:00-09:30 y 09:30-10:00
        assertThat(resultado.getSlots().get(0).isLibre()).isTrue();   // 09:00 libre
        assertThat(resultado.getSlots().get(1).isLibre()).isFalse();  // 09:30 ocupado (reservado)
        assertThat(resultado.isDisponible()).isTrue(); // hay al menos 1 libre
    }

    @Test
    void obtenerDisponibilidad_slotBloqueadoManualmente_debeMarcarseNoLibre() {
        LocalDate fecha = LocalDate.of(2026, 6, 1);
        HorarioDisponible horario = HorarioDisponible.builder()
                .diaSemana(fecha.getDayOfWeek())
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(9, 30))
                .duracionSlotMinutos(30).activo(true)
                .build();

        when(festivoRepo.existsByFecha(fecha)).thenReturn(false);
        when(horarioRepo.findByDiaSemanaAndActivoTrue(fecha.getDayOfWeek())).thenReturn(List.of(horario));
        when(bloqueoRepo.findByFechaOrderByHoraInicioAsc(fecha)).thenReturn(List.of());
        when(bloqueoRepo.existsBloqueoEnHorario(fecha, LocalTime.of(9, 0), LocalTime.of(9, 30))).thenReturn(true);
        when(disponibilidadClient.obtenerHorasReservadas(fecha)).thenReturn(Set.of());

        DisponibilidadDiaResponse resultado = agendaService.obtenerDisponibilidad(fecha);

        assertThat(resultado.getSlots()).hasSize(1);
        assertThat(resultado.getSlots().get(0).isLibre()).isFalse();
        assertThat(resultado.isDisponible()).isFalse();
    }

    // ---------- obtenerDisponibilidadRango ----------

    @Test
    void obtenerDisponibilidadRango_debeRetornarUnaEntradaPorDia() {
        LocalDate desde = LocalDate.of(2026, 6, 1);
        LocalDate hasta = LocalDate.of(2026, 6, 3);

        when(festivoRepo.existsByFecha(any())).thenReturn(true); // simplifica: todos festivos

        List<DisponibilidadDiaResponse> resultado = agendaService.obtenerDisponibilidadRango(desde, hasta);

        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(DisponibilidadDiaResponse::getFecha)
                .containsExactly(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 2),
                        LocalDate.of(2026, 6, 3)
                );
    }
}