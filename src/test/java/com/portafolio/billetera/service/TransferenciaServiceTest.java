package com.portafolio.billetera.service;

import com.portafolio.billetera.dto.request.TransferenciaRequest;
import com.portafolio.billetera.entity.Cuenta;
import com.portafolio.billetera.exception.OperacionInvalidaException;
import com.portafolio.billetera.exception.RecursoNoEncontradoException;
import com.portafolio.billetera.exception.SaldoInsuficienteException;
import com.portafolio.billetera.repository.CuentaRepository;
import com.portafolio.billetera.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/*
 * @ExtendWith(MockitoExtension.class): le dice a JUnit que use Mockito.
 * Los @Mock crean objetos "falsos" que simulan el comportamiento real
 * sin tocar la base de datos. Esto es lo que hace un test "unitario".
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferenciaService - Pruebas unitarias")
class TransferenciaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private TransferenciaService transferenciaService;

    private Cuenta cuentaOrigen;
    private Cuenta cuentaDestino;

    @BeforeEach
    void setUp() {
        // Preparamos datos de prueba limpios antes de cada test
        cuentaOrigen = new Cuenta();
        cuentaOrigen.setId(1L);
        cuentaOrigen.setNumeroCuenta("BV-0000000001");
        cuentaOrigen.setBalance(new BigDecimal("1000.00"));

        cuentaDestino = new Cuenta();
        cuentaDestino.setId(2L);
        cuentaDestino.setNumeroCuenta("BV-0000000002");
        cuentaDestino.setBalance(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Transferencia exitosa: debita origen y acredita destino correctamente")
    void transferir_cuandoHaySaldo_debeTransferirCorrectamente() {
        // ARRANGE (preparar): configuramos qué deben devolver los mocks
        when(cuentaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(cuentaDestino));
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new TransferenciaRequest(1L, 2L, new BigDecimal("300.00"), "Pago de prueba");

        // ACT (actuar): ejecutamos el método que queremos probar
        var response = transferenciaService.transferir(request);

        // ASSERT (verificar): comprobamos los resultados
        assertThat(cuentaOrigen.getBalance()).isEqualByComparingTo("700.00");
        assertThat(cuentaDestino.getBalance()).isEqualByComparingTo("800.00");
        assertThat(response.monto()).isEqualByComparingTo("300.00");

        // Verificamos que se guardaron los cambios (2 cuentas + 1 transacción)
        verify(cuentaRepository, times(2)).save(any(Cuenta.class));
        verify(transaccionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Transferencia falla: lanza SaldoInsuficienteException si no hay fondos")
    void transferir_cuandoNoHaySaldo_debeLanzarSaldoInsuficienteException() {
        when(cuentaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(cuentaDestino));
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuentaDestino));

        // Intentamos transferir más de lo que tiene la cuenta origen (1000.00)
        var request = new TransferenciaRequest(1L, 2L, new BigDecimal("9999.00"), "Transferencia imposible");

        // assertThatThrownBy verifica que el método lanza exactamente esta excepción
        assertThatThrownBy(() -> transferenciaService.transferir(request))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessageContaining("Saldo insuficiente");

        // Verificamos que no se guardó NADA (el rollback se habría dado en la BD real)
        verify(cuentaRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transferencia falla: lanza OperacionInvalidaException en auto-transferencia")
    void transferir_cuandoMismaCuenta_debeLanzarOperacionInvalidaException() {
        var request = new TransferenciaRequest(1L, 1L, new BigDecimal("100.00"), "Auto-transferencia");

        assertThatThrownBy(() -> transferenciaService.transferir(request))
                .isInstanceOf(OperacionInvalidaException.class)
                .hasMessageContaining("a ti mismo");

        // Si la validación falla antes, nunca debemos tocar la BD
        verify(cuentaRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    @DisplayName("Transferencia falla: lanza RecursoNoEncontradoException si la cuenta no existe")
    void transferir_cuandoCuentaNoExiste_debeLanzarRecursoNoEncontradoException() {
        when(cuentaRepository.findByIdWithLock(anyLong())).thenReturn(Optional.empty());

        var request = new TransferenciaRequest(1L, 99L, new BigDecimal("100.00"), "Cuenta fantasma");

        assertThatThrownBy(() -> transferenciaService.transferir(request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Cuenta no encontrada");
    }
}
