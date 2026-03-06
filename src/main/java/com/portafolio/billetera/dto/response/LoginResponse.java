package com.portafolio.billetera.dto.response;

public record LoginResponse(
        String token,
        Long usuarioId,
        String nombre,
        String email
) {}