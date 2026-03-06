package com.portafolio.billetera.config;

import com.portafolio.billetera.entity.Cuenta;
import com.portafolio.billetera.entity.Usuario;
import com.portafolio.billetera.repository.CuentaRepository;
import com.portafolio.billetera.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/*
  CommandLineRunner: Para cargar datos de prueba.
  @Profile("!prod"): este seeder no corre en producción.
  Solo corre en los perfiles "default" y "dev".
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DataSeeder.class);
    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("DataSeeder: datos ya existentes, se omite la carga inicial.");
            return;
        }

        log.info("DataSeeder: cargando datos de prueba...");

        crearUsuarioConCuenta("Alice Pérez",   "alice@demo.com", "password123", "BV-0000000001", new BigDecimal("1000.00"));
        crearUsuarioConCuenta("Bob Rodríguez", "bob@demo.com",   "password123", "BV-0000000002", new BigDecimal("500.00"));

        log.info("DataSeeder: ¡Listo! Usuarios creados:");
        log.info("  → alice@demo.com / password123 | Balance: S/. 1000.00");
        log.info("  → bob@demo.com   / password123 | Balance: S/.  500.00");
    }

    private void crearUsuarioConCuenta(String nombre, String email,
                                       String password, String numeroCuenta,
                                       BigDecimal balanceInicial) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuarioRepository.save(usuario);

        Cuenta cuenta = new Cuenta();
        cuenta.setUsuario(usuario);
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setBalance(balanceInicial);
        cuentaRepository.save(cuenta);
    }
}