package com.portafolio.billetera;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/*
 * Test de integración: verifica que el contexto completo de Spring
 * arranca sin errores (beans, configuración, seguridad, etc.).
 *
 * @ActiveProfiles("test"): usa application-test.yaml con H2 en memoria.
 * Así este test corre en cualquier máquina sin necesitar Docker.
 */
@SpringBootTest
@ActiveProfiles("test")
class BilleteraApplicationTests {

	@Test
	void contextLoads() {

	}
}