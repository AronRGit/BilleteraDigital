-- ============================================================
-- V1__crear_tablas.sql
-- Flyway ejecuta este script UNA sola vez, en orden de versión.
-- Nunca lo modifiques después de ejecutarlo. Si necesitas cambios,
-- crea V2__nombre_del_cambio.sql
-- ============================================================

CREATE TABLE usuarios (
    id         BIGSERIAL PRIMARY KEY,
    nombre     VARCHAR(100)        NOT NULL,
    email      VARCHAR(150)        NOT NULL UNIQUE,
    password   VARCHAR(255)        NOT NULL,  -- hash Argon2, nunca texto plano
    created_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE cuentas (
    id             BIGSERIAL PRIMARY KEY,
    usuario_id     BIGINT              NOT NULL UNIQUE,  -- 1 usuario = 1 cuenta
    numero_cuenta  VARCHAR(20)         NOT NULL UNIQUE,
    balance        NUMERIC(19, 4)      NOT NULL DEFAULT 0,
    version        BIGINT              NOT NULL DEFAULT 0, -- Optimistic Locking
    created_at     TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cuenta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT chk_balance_positivo CHECK (balance >= 0)   -- integridad a nivel BD
);

CREATE TABLE transacciones (
    id                 BIGSERIAL PRIMARY KEY,
    cuenta_origen_id   BIGINT              ,  -- NULL si es un depósito externo
    cuenta_destino_id  BIGINT              ,
    tipo               VARCHAR(30)         NOT NULL,
    monto              NUMERIC(19, 4)      NOT NULL,
    descripcion        VARCHAR(255),
    created_at         TIMESTAMP           NOT NULL DEFAULT NOW(),  -- inmutable por diseño

    CONSTRAINT fk_transaccion_origen  FOREIGN KEY (cuenta_origen_id)  REFERENCES cuentas(id),
    CONSTRAINT fk_transaccion_destino FOREIGN KEY (cuenta_destino_id) REFERENCES cuentas(id),
    CONSTRAINT chk_monto_positivo     CHECK (monto > 0)
);

-- Índices para acelerar consultas frecuentes del historial
CREATE INDEX idx_transacciones_origen  ON transacciones(cuenta_origen_id);
CREATE INDEX idx_transacciones_destino ON transacciones(cuenta_destino_id);
CREATE INDEX idx_transacciones_fecha   ON transacciones(created_at DESC);