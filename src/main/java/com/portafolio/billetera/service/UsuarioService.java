package com.portafolio.billetera.service;

import com.portafolio.billetera.dto.request.RegistroRequest;
import com.portafolio.billetera.dto.response.UsuarioResponse;
import com.portafolio.billetera.entity.Cuenta;
import com.portafolio.billetera.entity.Usuario;
import com.portafolio.billetera.exception.OperacionInvalidaException;
import com.portafolio.billetera.repository.CuentaRepository;
import com.portafolio.billetera.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder; // Argon2, inyectado desde PasswordEncoderConfig

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        log.info("Registrando nuevo usuario con email: {}", request.email());

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new OperacionInvalidaException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        // Nunca guardamos el password en texto plano
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuarioRepository.save(usuario);

        Cuenta cuenta = new Cuenta();
        cuenta.setUsuario(usuario);
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setBalance(BigDecimal.ZERO);
        cuentaRepository.save(cuenta);

        log.info("Usuario registrado. Cuenta creada: {}", cuenta.getNumeroCuenta());
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(),
                usuario.getEmail(), cuenta.getNumeroCuenta());
    }

    private String generarNumeroCuenta() {
        return "BV-" + String.format("%010d", (long) (Math.random() * 9_999_999_999L));
    }
}