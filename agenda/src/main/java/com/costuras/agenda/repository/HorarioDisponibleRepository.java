package com.costuras.agenda.repository;

import com.costuras.agenda.model.HorarioDisponible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface HorarioDisponibleRepository extends JpaRepository<HorarioDisponible, Integer> {
    List<HorarioDisponible> findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc();
    List<HorarioDisponible> findByDiaSemanaAndActivoTrue(DayOfWeek diaSemana);
}
