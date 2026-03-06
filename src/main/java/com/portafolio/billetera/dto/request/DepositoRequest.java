package com.portafolio.billetera.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositoRequest(
        @NotNull(message = "La cuenta destino es obligatoria")
        Long cuentaDestinoId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto mínimo es 0.01")
        BigDecimal monto
) {}