CREATE INDEX IF NOT EXISTS idx_outbox_status_created_ordered
ON outbox_events (status, created_at)
WHERE status = 'CREATED';