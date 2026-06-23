package com.costuras.agenda.repository;

import com.costuras.agenda.model.BloqueoManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BloqueoManualRepository extends JpaRepository<BloqueoManual, Integer> {

    List<BloqueoManual> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    List<BloqueoManual> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);

    
    
     
    @Query("""
        SELECT COUNT(b) > 0 FROM BloqueoManual b
        WHERE b.fecha = :fecha
          AND (
            b.horaInicio IS NULL
            OR (b.horaInicio <= :horaFin AND b.horaFin >= :horaInicio)
          )
    """)
    boolean existsBloqueoEnHorario(
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin
    );
}
