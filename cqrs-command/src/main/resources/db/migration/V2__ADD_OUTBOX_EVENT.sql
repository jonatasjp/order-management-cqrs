CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL, -- Ex: 'Order'
    aggregate_id UUID NOT NULL,           -- ID da order relacionada
    event_type VARCHAR(255) NOT NULL,     -- Ex: 'OrderCreated'
    payload JSONB NOT NULL,               -- Dados do evento
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent BOOLEAN NOT NULL DEFAULT FALSE,  -- Alterado para true, apenas quando o evento for enviado
    sent_at TIMESTAMP                     -- Preenchido apenas quando o evento for enviado
);

CREATE INDEX idx_outbox_unsent ON outbox_events(id) WHERE sent = false;
