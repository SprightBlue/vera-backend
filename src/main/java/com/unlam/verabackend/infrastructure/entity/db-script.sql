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

                            CONSTRAINT fk_analyses_user FOREIGN KEY (user_id)
                                REFERENCES public.users (id) ON DELETE CASCADE
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

                        CONSTRAINT fk_alerts_trust_contact FOREIGN KEY (trust_contact_id)
                            REFERENCES public.trust_contacts (id) ON DELETE CASCADE
);

CREATE INDEX idx_alerts_trust_contact_id ON public.alerts (trust_contact_id);
CREATE INDEX idx_alerts_is_resolved ON public.alerts (is_resolved);
CREATE INDEX idx_alerts_risk_level ON public.alerts (risk_level);


CREATE TABLE notifications (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id BIGINT NOT NULL,
                                type VARCHAR(50),
                                title VARCHAR(255),
                                message TEXT,
                                payload JSONB,
                                is_read BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)
                                    REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_created ON public.notifications (user_id, created_at DESC);