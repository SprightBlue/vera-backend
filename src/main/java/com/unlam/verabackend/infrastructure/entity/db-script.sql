CREATE TABLE public.analysis (
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

                                 CONSTRAINT fk_analyses_user FOREIGN KEY (user_id) REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE TABLE alerts (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               trust_contact_id BIGINT NOT NULL,

    -- Meta-información del hecho (Snapshot inmutable para métricas)
                               title VARCHAR(255),
                               source VARCHAR(255),
                               content_summary TEXT,
                               risk_type VARCHAR(50),
                               risk_level VARCHAR(50),
                               risk_percentage INT CHECK (risk_percentage BETWEEN 0 AND 100),
                               suspicious_patterns TEXT,

    -- Estados y Auditoría
                               is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               resolved_at TIMESTAMP WITHOUT TIME ZONE,

    -- Mantenemos únicamente la relación con el cuidador
                               CONSTRAINT fk_alerts_trust_contact FOREIGN KEY (trust_contact_id)
                                   REFERENCES public.trust_contacts (id) ON DELETE CASCADE
);

-- Índices esenciales para el buzón unificado y reportes analíticos rápidos
CREATE INDEX idx_alerts_trust_contact_id ON public.alerts (trust_contact_id);
CREATE INDEX idx_alerts_is_resolved ON public.alerts (is_resolved);
CREATE INDEX idx_alerts_risk_level ON public.alerts (risk_level); -- Clave para tus futuras métricas