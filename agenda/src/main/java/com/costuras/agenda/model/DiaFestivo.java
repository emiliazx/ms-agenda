package com.costuras.agenda.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


 
@Entity
@Table(name = "dias_festivos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaFestivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(nullable = false)
    private String descripcion;
}
