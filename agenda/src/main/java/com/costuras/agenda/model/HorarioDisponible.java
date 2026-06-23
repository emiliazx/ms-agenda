package com.costuras.agenda.model;


import java.time.DayOfWeek;
import java.time.LocalTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


 //Define el horario semanal de atención (ej: Lunes 09:00-18:00).
 //El admin configura bloques de disponibilidad por día de semana.
 
@Entity
@Table(name = "horarios_disponibles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDisponible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

   
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek diaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;


    @Column(nullable = false)
    private Integer duracionSlotMinutos;

   
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
