package com.portafolio.billetera.dto.response;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String numeroCuenta
) {}