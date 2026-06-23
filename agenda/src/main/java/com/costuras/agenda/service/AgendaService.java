package com.costuras.agenda.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.costuras.agenda.dto.BloqueoRequest;
import com.costuras.agenda.dto.BloqueoResponse;
import com.costuras.agenda.dto.DiaFestivoRequest;
import com.costuras.agenda.dto.DiaFestivoResponse;
import com.costuras.agenda.dto.DisponibilidadDiaResponse;
import com.costuras.agenda.dto.HorarioRequest;
import com.costuras.agenda.dto.HorarioResponse;
import com.costuras.agenda.model.BloqueoManual;
import com.costuras.agenda.model.DiaFestivo;
import com.costuras.agenda.model.HorarioDisponible;
import com.costuras.agenda.repository.BloqueoManualRepository;
import com.costuras.agenda.repository.DiaFestivoRepository;
import com.costuras.agenda.repository.HorarioDisponibleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class AgendaService {

    private final HorarioDisponibleRepository horarioRepo;
    private final DiaFestivoRepository festivoRepo;
    private final BloqueoManualRepository bloqueoRepo;
    private final DisponibilidadClient disponibilidadClient;

  

    public List<HorarioResponse> listarHorarios() {
        return horarioRepo.findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc()
                .stream().map(this::toHorarioResponse).toList();
    }

    public HorarioResponse crearHorario(HorarioRequest req) {
        if (req.getHoraFin().isBefore(req.getHoraInicio()) || req.getHoraFin().equals(req.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
        HorarioDisponible horario = HorarioDisponible.builder()
                .diaSemana(req.getDiaSemana())
                .horaInicio(req.getHoraInicio())
                .horaFin(req.getHoraFin())
                .duracionSlotMinutos(req.getDuracionSlotMinutos())
                .activo(true)
                .build();
        return toHorarioResponse(horarioRepo.save(horario));
    }

    public HorarioResponse actualizarHorario(Integer id, HorarioRequest req) {
        HorarioDisponible horario = horarioRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Horario no encontrado: " + id));
        horario.setDiaSemana(req.getDiaSemana());
        horario.setHoraInicio(req.getHoraInicio());
        horario.setHoraFin(req.getHoraFin());
        horario.setDuracionSlotMinutos(req.getDuracionSlotMinutos());
        return toHorarioResponse(horarioRepo.save(horario));
    }

    @Transactional
    public Map<String, String> eliminarHorario(Integer id) {
        HorarioDisponible horario = horarioRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Horario no encontrado: " + id));
        horario.setActivo(false);
        horarioRepo.save(horario);
        return Map.of("mensaje", "Horario desactivado correctamente");
    }

   

    public List<DiaFestivoResponse> listarFestivos() {
        return festivoRepo.findAllByOrderByFechaAsc()
                .stream().map(this::toFestivoResponse).toList();
    }

    public DiaFestivoResponse crearFestivo(DiaFestivoRequest req) {
        if (festivoRepo.existsByFecha(req.getFecha())) {
            throw new IllegalArgumentException("Ya existe un día festivo para la fecha: " + req.getFecha());
        }
        DiaFestivo festivo = DiaFestivo.builder()
                .fecha(req.getFecha())
                .descripcion(req.getDescripcion())
                .build();
        return toFestivoResponse(festivoRepo.save(festivo));
    }

    public Map<String, String> eliminarFestivo(Integer id) {
        festivoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Día festivo no encontrado: " + id));
        festivoRepo.deleteById(id);
        return Map.of("mensaje", "Día festivo eliminado correctamente");
    }

    public List<BloqueoResponse> listarBloqueos(LocalDate desde, LocalDate hasta) {
        List<BloqueoManual> bloqueos = (desde != null && hasta != null)
                ? bloqueoRepo.findByFechaBetweenOrderByFechaAscHoraInicioAsc(desde, hasta)
                : bloqueoRepo.findAll();
        return bloqueos.stream().map(this::toBloqueoResponse).toList();
    }

    public BloqueoResponse crearBloqueo(BloqueoRequest req) {
        BloqueoManual bloqueo = BloqueoManual.builder()
                .fecha(req.getFecha())
                .horaInicio(req.getHoraInicio())
                .horaFin(req.getHoraFin())
                .motivo(req.getMotivo())
                .build();
        return toBloqueoResponse(bloqueoRepo.save(bloqueo));
    }

    public Map<String, String> eliminarBloqueo(Integer id) {
        bloqueoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bloqueo no encontrado: " + id));
        bloqueoRepo.deleteById(id);
        return Map.of("mensaje", "Bloqueo eliminado correctamente");
    }

 

   
    
    public DisponibilidadDiaResponse obtenerDisponibilidad(LocalDate fecha) {
       
        if (festivoRepo.existsByFecha(fecha)) {
            return DisponibilidadDiaResponse.builder()
                    .fecha(fecha)
                    .disponible(false)
                    .motivo("Día festivo")
                    .slots(List.of())
                    .build();
        }

        
        List<HorarioDisponible> horarios = horarioRepo.findByDiaSemanaAndActivoTrue(fecha.getDayOfWeek());
        if (horarios.isEmpty()) {
            return DisponibilidadDiaResponse.builder()
                    .fecha(fecha)
                    .disponible(false)
                    .motivo("Sin horario de atención para este día")
                    .slots(List.of())
                    .build();
        }

       
        List<BloqueoManual> bloqueosDelDia = bloqueoRepo.findByFechaOrderByHoraInicioAsc(fecha);
        boolean bloqueoDia = bloqueosDelDia.stream().anyMatch(b -> b.getHoraInicio() == null);
        if (bloqueoDia) {
            String motivo = bloqueosDelDia.stream()
                    .filter(b -> b.getHoraInicio() == null)
                    .findFirst()
                    .map(b -> b.getMotivo() != null ? b.getMotivo() : "Día bloqueado")
                    .orElse("Día bloqueado");
            return DisponibilidadDiaResponse.builder()
                    .fecha(fecha)
                    .disponible(false)
                    .motivo(motivo)
                    .slots(List.of())
                    .build();
        }

     
        Set<LocalTime> horasOcupadas = disponibilidadClient.obtenerHorasReservadas(fecha);

       
        List<DisponibilidadDiaResponse.SlotHorario> slots = new ArrayList<>();
        for (HorarioDisponible horario : horarios) {
            LocalTime cursor = horario.getHoraInicio();
            while (cursor.plusMinutes(horario.getDuracionSlotMinutos()).compareTo(horario.getHoraFin()) <= 0) {
                LocalTime slotFin = cursor.plusMinutes(horario.getDuracionSlotMinutos());
                LocalTime slotInicio = cursor;

                boolean bloqueado = bloqueoRepo.existsBloqueoEnHorario(fecha, slotInicio, slotFin);
                boolean ocupado = horasOcupadas.contains(slotInicio);

                slots.add(DisponibilidadDiaResponse.SlotHorario.builder()
                        .horaInicio(slotInicio)
                        .horaFin(slotFin)
                        .libre(!bloqueado && !ocupado)
                        .build());
                cursor = slotFin;
            }
        }

        return DisponibilidadDiaResponse.builder()
                .fecha(fecha)
                .disponible(slots.stream().anyMatch(DisponibilidadDiaResponse.SlotHorario::isLibre))
                .motivo(null)
                .slots(slots)
                .build();
    }

   
    
    public List<DisponibilidadDiaResponse> obtenerDisponibilidadRango(LocalDate desde, LocalDate hasta) {
        List<DisponibilidadDiaResponse> resultado = new ArrayList<>();
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            resultado.add(obtenerDisponibilidad(cursor));
            cursor = cursor.plusDays(1);
        }
        return resultado;
    }

   

    private HorarioResponse toHorarioResponse(HorarioDisponible h) {
        return HorarioResponse.builder()
                .id(h.getId())
                .diaSemana(h.getDiaSemana())
                .horaInicio(h.getHoraInicio())
                .horaFin(h.getHoraFin())
                .duracionSlotMinutos(h.getDuracionSlotMinutos())
                .activo(h.getActivo())
                .build();
    }

    private DiaFestivoResponse toFestivoResponse(DiaFestivo f) {
        return DiaFestivoResponse.builder()
                .id(f.getId())
                .fecha(f.getFecha())
                .descripcion(f.getDescripcion())
                .build();
    }

    private BloqueoResponse toBloqueoResponse(BloqueoManual b) {
        return BloqueoResponse.builder()
                .id(b.getId())
                .fecha(b.getFecha())
                .horaInicio(b.getHoraInicio())
                .horaFin(b.getHoraFin())
                .motivo(b.getMotivo())
                .build();
    }
}
