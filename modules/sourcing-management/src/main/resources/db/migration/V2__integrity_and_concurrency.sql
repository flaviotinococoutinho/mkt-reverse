-- ============================================================
-- MIGRATION: Add concurrency and data integrity constraints
-- Author: mkt-reverse
-- Date: 2026-04-11
--
-- Fresh-database safe: no MVP o schema base é criado pelo Hibernate
-- (ddl-auto) DEPOIS do Flyway, então cada bloco só executa se a tabela
-- alvo já existir (to_regclass). Em bancos novos esta migração é no-op
-- e os índices/constraints equivalentes são criados pelo mapeamento JPA.
-- ============================================================

DO $$
BEGIN
    IF to_regclass('src_sourcing_events') IS NOT NULL THEN
        -- 1. Optimistic locking version column
        ALTER TABLE src_sourcing_events
            ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
        COMMENT ON COLUMN src_sourcing_events.version IS 'Optimistic locking version';

        -- Status transition tracking
        ALTER TABLE src_sourcing_events
            ADD COLUMN IF NOT EXISTS status_changed_at TIMESTAMP;
        ALTER TABLE src_sourcing_events
            ADD COLUMN IF NOT EXISTS status_changed_by VARCHAR(255);

        -- Audit timestamps
        ALTER TABLE src_sourcing_events
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        ALTER TABLE src_sourcing_events
            ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

        -- Search/browse indexes
        CREATE INDEX IF NOT EXISTS idx_sourcing_events_tenant_status
            ON src_sourcing_events (tenant_id, status);
        CREATE INDEX IF NOT EXISTS idx_sourcing_events_published_at
            ON src_sourcing_events (published_at DESC);
        CREATE INDEX IF NOT EXISTS idx_sourcing_events_title_fts
            ON src_sourcing_events USING gin(to_tsvector('portuguese', coalesce(title, '')));
        CREATE INDEX IF NOT EXISTS idx_sourcing_events_description_fts
            ON src_sourcing_events USING gin(to_tsvector('portuguese', coalesce(description, '')));
    END IF;

    IF to_regclass('src_supplier_responses') IS NOT NULL THEN
        -- Unique proposal per supplier per intent (sealed-proposal guardrail)
        BEGIN
            ALTER TABLE src_supplier_responses
                ADD CONSTRAINT uk_event_supplier UNIQUE (sourcing_event_id, supplier_id);
        EXCEPTION
            WHEN duplicate_table THEN NULL;
            WHEN duplicate_object THEN NULL;
            WHEN undefined_column THEN NULL;
        END;

        ALTER TABLE src_supplier_responses
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        ALTER TABLE src_supplier_responses
            ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

        CREATE INDEX IF NOT EXISTS idx_supplier_responses_event_id
            ON src_supplier_responses (sourcing_event_id);
        CREATE INDEX IF NOT EXISTS idx_supplier_responses_supplier_id
            ON src_supplier_responses (supplier_id);
    END IF;

    IF to_regclass('usr_users') IS NOT NULL THEN
        BEGIN
            ALTER TABLE usr_users
                ADD CONSTRAINT uk_user_email_tenant UNIQUE (tenant_id, email);
        EXCEPTION
            WHEN duplicate_table THEN NULL;
            WHEN duplicate_object THEN NULL;
            WHEN undefined_column THEN NULL;
        END;
    END IF;
END
$$;
