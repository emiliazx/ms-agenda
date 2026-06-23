package com.costuras.agenda.dto;




import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoRequest {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

  
    private LocalTime horaInicio;

    
    private LocalTime horaFin;

    private String motivo;
}
