package com.costuras.agenda.client;

import com.costuras.agenda.service.DisponibilidadClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

import java.util.Set;


@Slf4j
@Component
public class DisponibilidadClientImpl implements DisponibilidadClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DisponibilidadClientImpl(
            @Value("${ms.disponibilidad.url}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<LocalTime> obtenerHorasReservadas(LocalDate fecha) {
        try {
            String url = baseUrl + "/reservas/horas-ocupadas?fecha=" + fecha;
            List<String> horas = restTemplate.getForObject(url, List.class);
            if (horas == null) return Set.of();
            Set<LocalTime> resultado = new HashSet<>();
            for (String h : horas) {
                resultado.add(LocalTime.parse(h));
            }
            return resultado;
        } catch (Exception e) {
            log.warn("No se pudo consultar horas ocupadas en MS-Disponibilidad: {}", e.getMessage());
           
            return Set.of();
        }
    }
}
