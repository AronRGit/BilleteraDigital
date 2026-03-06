package com.portafolio.billetera.repository;

import com.portafolio.billetera.entity.Transaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Page<Transaccion> findByCuentaOrigenIdOrCuentaDestinoIdOrderByCreatedAtDesc(
            Long cuentaOrigenId,
            Long cuentaDestinoId,
            Pageable pageable
    );
}