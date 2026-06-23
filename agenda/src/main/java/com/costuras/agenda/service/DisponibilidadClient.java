package com.costuras.agenda.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public interface DisponibilidadClient {
    Set<LocalTime> obtenerHorasReservadas(LocalDate fecha);
}
