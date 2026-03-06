package com.portafolio.billetera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Getter
@NoArgsConstructor
public class Transaccion {

    public enum Tipo {
        DEPOSITO,
        TRANSFERENCIA_ENVIADA,
        TRANSFERENCIA_RECIBIDA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id")
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    private Cuenta cuentaDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tipo tipo;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    private Transaccion(Cuenta origen, Cuenta destino, Tipo tipo,
                        BigDecimal monto, String descripcion) {
        this.cuentaOrigen  = origen;
        this.cuentaDestino = destino;
        this.tipo          = tipo;
        this.monto         = monto;
        this.descripcion   = descripcion;
    }

    public static Transaccion deposito(Cuenta destino, BigDecimal monto) {
        return new Transaccion(null, destino, Tipo.DEPOSITO, monto, "Depósito");
    }

    public static Transaccion transferencia(Cuenta origen, Cuenta destino,
                                            BigDecimal monto, String descripcion) {
        return new Transaccion(origen, destino, Tipo.TRANSFERENCIA_ENVIADA, monto, descripcion);
    }
}