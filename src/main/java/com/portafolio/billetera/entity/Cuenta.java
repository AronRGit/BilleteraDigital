package com.portafolio.billetera.entity;

import com.portafolio.billetera.exception.SaldoInsuficienteException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
@NoArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public void acreditar(BigDecimal monto) {
        this.balance = this.balance.add(monto);
    }

    public void debitar(BigDecimal monto) {
        if (this.balance.compareTo(monto) < 0) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente. Balance actual: " + this.balance + ", Monto solicitado: " + monto
            );
        }
        this.balance = this.balance.subtract(monto);
    }
}