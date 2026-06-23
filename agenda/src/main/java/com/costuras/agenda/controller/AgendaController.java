package com.costuras.agenda.controller;


import com.costuras.agenda.dto.BloqueoRequest;
import com.costuras.agenda.dto.BloqueoResponse;
import com.costuras.agenda.dto.DiaFestivoRequest;
import com.costuras.agenda.dto.DiaFestivoResponse;
import com.costuras.agenda.dto.DisponibilidadDiaResponse;
import com.costuras.agenda.dto.HorarioRequest;
import com.costuras.agenda.dto.HorarioResponse;
import com.costuras.agenda.service.AgendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService agendaService;

   
    @GetMapping("/horarios")
    public ResponseEntity<List<HorarioResponse>> listarHorarios() {
        return ResponseEntity.ok(agendaService.listarHorarios());
    }
    @Operation(
        summary = "Endpoint para renovar el token de autenticación",
        description = "Este endpoint permite a los usuarios renovar su token de autenticación utilizando un token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    }) 

    
    @PostMapping("/horarios")
    public ResponseEntity<HorarioResponse> crearHorario(@Valid @RequestBody HorarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaService.crearHorario(req));
    }

  
    @PutMapping("/horarios/{id}")
    public ResponseEntity<HorarioResponse> actualizarHorario(
            @PathVariable Integer id,
            @Valid @RequestBody HorarioRequest req) {
        return ResponseEntity.ok(agendaService.actualizarHorario(id, req));
    }

    
    @DeleteMapping("/horarios/{id}")
    public ResponseEntity<Map<String, String>> eliminarHorario(@PathVariable Integer id) {
        return ResponseEntity.ok(agendaService.eliminarHorario(id));
    }

   

   
    @GetMapping("/festivos")
    public ResponseEntity<List<DiaFestivoResponse>> listarFestivos() {
        return ResponseEntity.ok(agendaService.listarFestivos());
    }

  
    @PostMapping("/festivos")
    public ResponseEntity<DiaFestivoResponse> crearFestivo(@Valid @RequestBody DiaFestivoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaService.crearFestivo(req));
    }

   
    @DeleteMapping("/festivos/{id}")
    public ResponseEntity<Map<String, String>> eliminarFestivo(@PathVariable Integer id) {
        return ResponseEntity.ok(agendaService.eliminarFestivo(id));
    }

   
    @GetMapping("/bloqueos")
    public ResponseEntity<List<BloqueoResponse>> listarBloqueos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(agendaService.listarBloqueos(desde, hasta));
    }

    
    @PostMapping("/bloqueos")
    public ResponseEntity<BloqueoResponse> crearBloqueo(@Valid @RequestBody BloqueoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaService.crearBloqueo(req));
    }

    
    @DeleteMapping("/bloqueos/{id}")
    public ResponseEntity<Map<String, String>> eliminarBloqueo(@PathVariable Integer id) {
        return ResponseEntity.ok(agendaService.eliminarBloqueo(id));
    }

   
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadDiaResponse> obtenerDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(agendaService.obtenerDisponibilidad(fecha));
    }

    
    
    
    @GetMapping("/disponibilidad/rango")
    public ResponseEntity<List<DisponibilidadDiaResponse>> obtenerDisponibilidadRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(agendaService.obtenerDisponibilidadRango(desde, hasta));
    }
}
