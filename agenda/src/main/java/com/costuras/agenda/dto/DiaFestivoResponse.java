package com.costuras.agenda.dto;



import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaFestivoResponse {
    private Integer id;
    private LocalDate fecha;
    private String descripcion;
}
