package com.costuras.agenda.repository;

import com.costuras.agenda.model.DiaFestivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaFestivoRepository extends JpaRepository<DiaFestivo, Integer> {
    boolean existsByFecha(LocalDate fecha);
    List<DiaFestivo> findAllByOrderByFechaAsc();
    List<DiaFestivo> findByFechaBetweenOrderByFechaAsc(LocalDate desde, LocalDate hasta);
}
