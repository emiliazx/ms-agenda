package com.costuras.agenda.dto;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadDiaResponse {

    private LocalDate fecha;
    private boolean disponible;
    
    private String motivo;
    
    private List<SlotHorario> slots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotHorario {
        private LocalTime horaInicio;
        private LocalTime horaFin;
       
        private boolean libre;
    }
}
