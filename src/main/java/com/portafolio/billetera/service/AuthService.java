package com.portafolio.billetera.service;

import com.portafolio.billetera.dto.request.LoginRequest;
import com.portafolio.billetera.dto.response.LoginResponse;
import com.portafolio.billetera.exception.OperacionInvalidaException;
import com.portafolio.billetera.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        // 1. Buscamos el usuario por email
        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new OperacionInvalidaException("Credenciales inválidas"));

        // 2. Verificamos la contraseña con Argon2
        // comparamos  con matches()
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new OperacionInvalidaException("Credenciales inválidas");
        }

        // 3. Generamos el token JWT
        // Usamos un SimpleUserDetails para no exponer la entidad Usuario a Spring Security
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generarToken(userDetails);

        return new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );
    }
}