CREATE TABLE analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(255),
    source VARCHAR(255),
    content_summary TEXT,
    risk_type VARCHAR(50),
    risk_level VARCHAR(50),
    risk_percentage INT CHECK (risk_percentage BETWEEN 0 AND 100),
    suspicious_patterns TEXT,
    recommendation TEXT,

    CONSTRAINT fk_analyses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trust_contact_id BIGINT NOT NULL,
    title VARCHAR(255),
    source VARCHAR(255),
    content_summary TEXT,
    risk_type VARCHAR(50),
    risk_level VARCHAR(50),
    risk_percentage INT CHECK (risk_percentage BETWEEN 0 AND 100),
    suspicious_patterns TEXT,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT fk_alerts_trust_contact FOREIGN KEY (trust_contact_id) REFERENCES trust_contacts (id) ON DELETE CASCADE
);

CREATE INDEX idx_alerts_trust_contact_id ON alerts (trust_contact_id);
CREATE INDEX idx_alerts_is_resolved ON alerts (is_resolved);
CREATE INDEX idx_alerts_risk_level ON alerts (risk_level);


CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    type VARCHAR(50),
    title VARCHAR(255),
    message TEXT,
    payload JSONB,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);

CREATE TABLE notification_device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    platform VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_device_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_device_tokens_user_active ON notification_device_tokens (user_id, active);


CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    analysis_id UUID,
    alert_id UUID,
    title VARCHAR(255) NOT NULL DEFAULT 'Consulta de Seguridad',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chats_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_chats_analysis FOREIGN KEY (analysis_id) REFERENCES analysis (id) ON DELETE SET NULL,
    CONSTRAINT fk_chats_alert FOREIGN KEY (alert_id) REFERENCES alerts (id) ON DELETE SET NULL
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL,
    role VARCHAR(50),
    content TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_messages_chat FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE
);

CREATE INDEX idx_chats_user ON chats (user_id);
CREATE INDEX idx_chat_messages_chat_order ON chat_messages (chat_id, created_at ASC);
