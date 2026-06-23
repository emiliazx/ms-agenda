package com.costuras.agenda.model;


import java.time.LocalDate;
import java.time.LocalTime;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


  
 
@Entity
@Table(name = "bloqueos_manuales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate fecha;

  
    private LocalTime horaInicio;

   
    private LocalTime horaFin;

    private String motivo;
}
