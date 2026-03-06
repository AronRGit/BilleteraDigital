package com.portafolio.billetera.service;

import com.portafolio.billetera.dto.request.DepositoRequest;
import com.portafolio.billetera.dto.request.TransferenciaRequest;
import com.portafolio.billetera.dto.response.TransaccionResponse;
import com.portafolio.billetera.entity.Cuenta;
import com.portafolio.billetera.entity.Transaccion;
import com.portafolio.billetera.exception.OperacionInvalidaException;
import com.portafolio.billetera.exception.RecursoNoEncontradoException;
import com.portafolio.billetera.repository.CuentaRepository;
import com.portafolio.billetera.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    @Transactional
    public TransaccionResponse depositar(DepositoRequest request) {
        log.info("Iniciando depósito de {} a cuenta ID {}", request.monto(), request.cuentaDestinoId());

        Cuenta destino = buscarCuentaOLanzarError(request.cuentaDestinoId());
        destino.acreditar(request.monto());
        cuentaRepository.save(destino);

        Transaccion transaccion = Transaccion.deposito(destino, request.monto());
        transaccionRepository.save(transaccion);

        log.info("Depósito exitoso. Nuevo balance cuenta {}: {}", destino.getNumeroCuenta(), destino.getBalance());
        return mapToResponse(transaccion);
    }

    @Transactional
    public TransaccionResponse transferir(TransferenciaRequest request) {
        log.info("Iniciando transferencia de {} | origen: {} → destino: {}",
                request.monto(), request.cuentaOrigenId(), request.cuentaDestinoId());

        validarQueNoSeaAutoTransferencia(request);

        Long primerLock  = Math.min(request.cuentaOrigenId(), request.cuentaDestinoId());
        Long segundoLock = Math.max(request.cuentaOrigenId(), request.cuentaDestinoId());

        cuentaRepository.findByIdWithLock(primerLock)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada: " + primerLock));
        cuentaRepository.findByIdWithLock(segundoLock)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada: " + segundoLock));

        Cuenta origen  = buscarCuentaOLanzarError(request.cuentaOrigenId());
        Cuenta destino = buscarCuentaOLanzarError(request.cuentaDestinoId());

        origen.debitar(request.monto());
        destino.acreditar(request.monto());

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        Transaccion transaccion = Transaccion.transferencia(origen, destino, request.monto(), request.descripcion());
        transaccionRepository.save(transaccion);

        log.info("Transferencia exitosa. Balance origen: {} | Balance destino: {}",
                origen.getBalance(), destino.getBalance());
        return mapToResponse(transaccion);
    }

    @Transactional(readOnly = true)
    public Page<TransaccionResponse> obtenerHistorial(Long cuentaId, Pageable pageable) {
        if (!cuentaRepository.existsById(cuentaId)) {
            throw new RecursoNoEncontradoException("Cuenta no encontrada: " + cuentaId);
        }
        return transaccionRepository
                .findByCuentaOrigenIdOrCuentaDestinoIdOrderByCreatedAtDesc(cuentaId, cuentaId, pageable)
                .map(this::mapToResponse);
    }

    private Cuenta buscarCuentaOLanzarError(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada: " + id));
    }

    private void validarQueNoSeaAutoTransferencia(TransferenciaRequest request) {
        if (request.cuentaOrigenId().equals(request.cuentaDestinoId())) {
            throw new OperacionInvalidaException("No puedes transferirte dinero a ti mismo");
        }
    }

    private TransaccionResponse mapToResponse(Transaccion t) {
        return new TransaccionResponse(
                t.getId(),
                t.getTipo().name(),
                t.getMonto(),
                t.getDescripcion(),
                t.getCreatedAt()
        );
    }
}