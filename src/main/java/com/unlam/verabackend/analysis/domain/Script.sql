-- ============================================================================
-- 1. CREACIÓN DE TABLAS DICCIONARIO (Estructuras de Soporte)
-- ============================================================================

-- Procedencia u origen del mensaje
CREATE TABLE message_sources (
                                 id VARCHAR(50) PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL
);

-- Niveles de riesgo de seguridad
CREATE TABLE risk_levels (
                             id VARCHAR(50) PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,
                             description VARCHAR(255)
);

-- ============================================================================
-- 2. CREACIÓN DE TABLAS DE NEGOCIO (Entidades Principales)
-- ============================================================================

-- Tabla de Mensajes recibidos
CREATE TABLE messages (
                          id UUID PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          source_id VARCHAR(50) NOT NULL,
                          received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_messages_source FOREIGN KEY (source_id) REFERENCES message_sources(id),
                          CONSTRAINT fk_messages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla de Análisis
CREATE TABLE analyses (
                          id UUID PRIMARY KEY,
                          message_id UUID NOT NULL,
                          is_threat BOOLEAN NOT NULL,
                          risk_level_id VARCHAR(50) NOT NULL,
                          suspicious_patterns TEXT NOT NULL,
                          recommendation TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_analyses_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
                          CONSTRAINT fk_analyses_risk_level FOREIGN KEY (risk_level_id) REFERENCES risk_levels(id)
);

-- ============================================================================
-- 3. INSERCIÓN DE DATOS MAESTROS
-- ============================================================================

-- Carga de Orígenes de Mensajes
INSERT INTO message_sources (id, name) VALUES
                                           ('UNKNOWN', 'Origen No Especificado'),
                                           ('WHATSAPP', 'WhatsApp'),
                                           ('TELEGRAM', 'Telegram');

-- Carga de Niveles de Riesgo
INSERT INTO risk_levels (id, name, description) VALUES
                                                    ('LOW', 'Bajo', 'No encontramos nada fuera de lo común en este mensaje. Podés interactuar con tranquilidad.'),
                                                    ('MEDIUM', 'Medio', 'Este mensaje tiene algunos detalles confusos o solicita cosas con mucha prisa. Te sugerimos mirarlo con atención y, ante la duda, charlarlo con alguien de confianza antes de responder.'),
                                                    ('HIGH', 'Alto', 'Detectamos que este mensaje contiene enlaces o pedidos falsos que podrían no ser seguros. Lo ideal es no hacer clic en los enlaces, evitar compartir datos y borrar el mensaje para estar más tranquilos.'),
                                                    ('UNDEFINED', 'Indefinido', 'No pudimos determinar con claridad el nivel de riesgo de este mensaje. Te sugerimos revisarlo con calma antes de actuar.');
