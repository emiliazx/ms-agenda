package com.costuras.agenda.controller;

import com.costuras.agenda.dto.*;
import com.costuras.agenda.service.AgendaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SuppressWarnings("null")
@WebMvcTest(controllers = AgendaController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class AgendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendaService agendaService;

    private ObjectMapper objectMapper;
    private HorarioResponse horarioResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        horarioResponse = new HorarioResponse();
        horarioResponse.setId(1);
        horarioResponse.setDiaSemana(DayOfWeek.MONDAY);
        horarioResponse.setHoraInicio(LocalTime.of(9, 0));
        horarioResponse.setHoraFin(LocalTime.of(17, 0));
        horarioResponse.setDuracionSlotMinutos(60);
        horarioResponse.setActivo(true);
    }

    @Test
    void listarHorarios_retorna200() throws Exception {
        when(agendaService.listarHorarios()).thenReturn(List.of(horarioResponse));

        mockMvc.perform(get("/agenda/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diaSemana").value("MONDAY"))
                .andExpect(jsonPath("$[0].duracionSlotMinutos").value(60));
    }

    @Test
    void crearHorario_datosValidos_retorna201() throws Exception {
        HorarioRequest request = new HorarioRequest();
        request.setDiaSemana(DayOfWeek.TUESDAY);
        request.setHoraInicio(LocalTime.of(9, 0));
        request.setHoraFin(LocalTime.of(17, 0));
        request.setDuracionSlotMinutos(60);

        when(agendaService.crearHorario(request)).thenReturn(horarioResponse);

        mockMvc.perform(post("/agenda/horarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
              
    }
    
    @Test
    void eliminarHorario_existente_retorna200() throws Exception {
        when(agendaService.eliminarHorario(1))
                .thenReturn(Map.of("mensaje", "Horario desactivado correctamente"));

        mockMvc.perform(delete("/agenda/horarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Horario desactivado correctamente"));
    }
    
    @Test
    void listarFestivos_retorna200() throws Exception {
        DiaFestivoResponse festivo = new DiaFestivoResponse();
        festivo.setId(1);
        festivo.setFecha(LocalDate.of(2025, 9, 18));
        festivo.setDescripcion("Fiestas Patrias");

        when(agendaService.listarFestivos()).thenReturn(List.of(festivo));

        mockMvc.perform(get("/agenda/festivos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Fiestas Patrias"));
    }
    
    @Test
    void crearFestivo_nuevo_retorna201() throws Exception {
        DiaFestivoRequest request = new DiaFestivoRequest();
        request.setFecha(LocalDate.of(2027, 10, 12));
        request.setDescripcion("Día de la Raza");

        DiaFestivoResponse resp = new DiaFestivoResponse();
        resp.setId(2);
        resp.setDescripcion("Día de la Raza");

        when(agendaService.crearFestivo(request)).thenReturn(resp);

        mockMvc.perform(post("/agenda/festivos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
                //.andExpect(jsonPath("$.descripcion").value("Día de la Raza"));
    }
    
    @Test
    void obtenerDisponibilidad_retorna200() throws Exception {
        DisponibilidadDiaResponse disp = new DisponibilidadDiaResponse();
        disp.setFecha(LocalDate.now().plusDays(1));
        disp.setDisponible(true);

        when(agendaService.obtenerDisponibilidad(any())).thenReturn(disp);

        mockMvc.perform(get("/agenda/disponibilidad")
                .param("fecha", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }
    
    @Test
    void crearBloqueo_datosValidos_retorna201() throws Exception {
        BloqueoRequest request = new BloqueoRequest();
        request.setFecha(LocalDate.now().plusDays(5));
        request.setMotivo("Capacitación");

        BloqueoResponse resp = new BloqueoResponse();
        resp.setId(1);
        resp.setMotivo("Capacitación");

        when(agendaService.crearBloqueo(request)).thenReturn(resp);

        mockMvc.perform(post("/agenda/bloqueos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                 .andExpect(jsonPath("$.motivo").value("Capacitación"));
    }
}