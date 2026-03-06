package com.portafolio.billetera.controller;

import com.portafolio.billetera.dto.request.DepositoRequest;
import com.portafolio.billetera.dto.request.TransferenciaRequest;
import com.portafolio.billetera.dto.response.TransaccionResponse;
import com.portafolio.billetera.service.TransferenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transferencias", description = "Operaciones de movimiento de dinero")
@RestController
@RequestMapping("/api/v1/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @Operation(summary = "Realizar un depósito", description = "Acredita dinero en una cuenta")
    @PostMapping("/deposito")
    public ResponseEntity<TransaccionResponse> depositar(@Valid @RequestBody DepositoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferenciaService.depositar(request));
    }

    @Operation(summary = "Realizar una transferencia", description = "Mueve dinero entre dos cuentas")
    @PostMapping
    public ResponseEntity<TransaccionResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferenciaService.transferir(request));
    }

    @Operation(summary = "Obtener historial", description = "Devuelve las transacciones de una cuenta de forma paginada")
    @GetMapping("/historial/{cuentaId}")
    public ResponseEntity<Page<TransaccionResponse>> obtenerHistorial(
            @PathVariable Long cuentaId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(transferenciaService.obtenerHistorial(cuentaId, pageable));
    }
}