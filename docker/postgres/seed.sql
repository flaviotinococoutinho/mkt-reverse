-- ============================================================
-- Seed Data - Dados de Teste para Desenvolvimento Local
-- ============================================================
-- Execute após as migrações (Flyway) para popular o banco com dados de teste
-- ============================================================

-- ----------------------------------------------------------
-- 1. Users de Teste
-- -----------------------------------------------------------

-- Buyer de teste
INSERT INTO usr_users (id, tenant_id, email, password_hash, first_name, last_name, display_name, document_type, document_number, user_type, status, created_at, updated_at)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'tenant-default', 'buyer@test.com', '$2a$10$rXKkZ8FqJ7jGhHJxUxFqF.ZwGvHJxUqFqF.ZwGvHJxU', 'João', 'Silva', 'João Silva', 'CPF', '12345678901', 'BUYER', 'ACTIVE', NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'tenant-default', 'buyer2@test.com', '$2a$10$rXKkZ8FqJ7jGhHJxUxFqF.ZwGvHJxUqFqF.ZwGvHJxU', 'Maria', 'Santos', 'Maria Santos', 'CPF', '98765432109', 'BUYER', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Supplier de teste
INSERT INTO usr_users (id, tenant_id, email, password_hash, first_name, last_name, display_name, document_type, document_number, user_type, status, created_at, updated_at)
VALUES 
    ('33333333-3333-3333-3333-333333333333', 'tenant-default', 'supplier@test.com', '$2a$10$rXKkZ8FqJ7jGhHJxUxFqF.ZwGvHJxUqFqF.ZwGvHJxU', 'Carlos', 'Oliveira', 'Carlos Oliveira', 'CNPJ', '12345678000199', 'SUPPLIER', 'ACTIVE', NOW(), NOW()),
    ('44444444-4444-4444-4444-444444444444', 'tenant-default', 'supplier2@test.com', '$2a$10$rXKkZ8FqJ7jGhHJxUxFqF.ZwGvHJxUqFqF.ZwGvHJxU', 'Ana', ' Pereira', 'Ana Pereira', 'CNPJ', '98765478000199', 'SUPPLIER', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------
-- 2. Organizations de Teste
-- -----------------------------------------------------------

INSERT INTO usr_organizations (id, tenant_id, name, document_type, document_number, status, created_at, updated_at)
VALUES 
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'tenant-default', 'Empresa João Silva ME', 'CNPJ', '12345678000199', 'ACTIVE', NOW(), NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'tenant-default', 'Empresa Maria Santos LTDA', 'CNPJ', '98765478000199', 'ACTIVE', NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'tenant-default', 'Carlos Equipamentos LTDA', 'CNPJ', '11111111000101', 'ACTIVE', NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'tenant-default', 'Ana Materiais LTDA', 'CNPJ', '22222222000102', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------
-- 3. Sourcing Events de Teste
-- -----------------------------------------------------------

-- Eventos Publicados (ativos para suppliers)
INSERT INTO src_sourcing_events (
    id, tenant_id, buyer_id, buyer_organization_id, buyer_contact_name, buyer_contact_phone, buyer_contact_email,
    title, description, event_type, product_name, product_description, mcc_category_code, unit_of_measure,
    quantity_required, estimated_budget, currency, status, visibility, published_at, expires_at, created_at, updated_at
)
VALUES 
    -- Evento 1: RFQ - Notebooks
    ('eeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'tenant-default', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'João Silva', '+5511999999999', 'buyer@test.com',
    'Compra de Notebooks Dell XPS 15', 'Precisamos de 10 notebooks Dell XPS 15 para renovação do parque tecnológico da empresa. Specs: i7, 16GB RAM, 512GB SSD, Windows 11 Pro.', 
    'RFQ', 'Notebook Dell XPS 15', 'Notebook Dell XPS 15 - i7, 16GB RAM, 512GB SSD', 174, 'un',
    10, 7500000, 'BRL', 'PUBLISHED', 'PUBLIC', NOW(), NOW() + INTERVAL '7 days', NOW(), NOW()),

    -- Evento 2: RFQ - Móveis
    ('ffffffff-ffff-ffff-ffff-ffff-ffffffffffff', 'tenant-default', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Maria Santos', '+5511999998888', 'buyer2@test.com',
    'Compra de Mobiliário para Escritório', 'Precisamos de 20 cadeiras ergonômicas, 5 mesas de reunião e 10 armários para novo escritório.', 
    'RFQ', 'Mobiliário Escritório', 'Cadeiras ergonômicas, mesas e armários', 521, 'un',
    35, 2500000, 'BRL', 'PUBLISHED', 'PUBLIC', NOW(), NOW() + INTERVAL '14 days', NOW(), NOW()),

    -- Evento 3: Reverse Auction - Autopeças
    ('11111111-1111-1111-1111-111111111110', 'tenant-default', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'João Silva', '+5511999999999', 'buyer@test.com',
    'Leilão Reverso - Filtros de Óleo Automotivos', 'Compra de 1000 filtros de óleo para frota da empresa. Melhor preço ganha.', 
    'REVERSE_AUCTION', 'Filtros de Óleo', 'Filtros de óleo automotivos padrão', 553, 'un',
    1000, 500000, 'BRL', 'PUBLISHED', 'PUBLIC', NOW(), NOW() + INTERVAL '3 days', NOW(), NOW()),

    -- Evento 4: Marketplace - Materiais
    ('22222222-2222-2222-2222-222222222220', 'tenant-default', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Maria Santos', '+5511999998888', 'buyer2@test.com',
    'Mercado - Parafusos e Porcas', 'Compra recorrente de parafusos e porcas para manutenção.', 
    'MARKETPLACE', 'Parafusos e Porcas', 'kits com parafusos e porcas variados', 525, 'kit',
    100, 50000, 'BRL', 'PUBLISHED', 'PUBLIC', NOW(), NOW() + INTERVAL '30 days', NOW(), NOW()),

    -- Evento 5: Em Andamento
    ('33333333-3333-3333-3333-333333333330', 'tenant-default', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'João Silva', '+5511999999999', 'buyer@test.com',
    'Compra de Impressoras', '5 impressoras Samsung para departamento.', 
    'RFQ', 'Impressora Samsung', 'Impressora laser colorida', 504, 'un',
    5, 1500000, 'BRL', 'IN_PROGRESS', 'PUBLIC', NOW() - INTERVAL '2 days', NOW() + INTERVAL '5 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------
-- 4. Supplier Responses (Propostas)
-- -----------------------------------------------------------

INSERT INTO src_supplier_responses (
    id, sourcing_event_id, supplier_id, supplier_organization_id, supplier_contact_name, supplier_contact_phone, supplier_contact_email,
    offer_cents, lead_time_days, warranty_months, condition, shipping_mode, message, status, submitted_at, created_at, updated_at
)
VALUES 
    -- Propostas para Evento 1 (Notebooks)
    ('resp-111-111-111-111-111111111111', 'eeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Carlos Oliveira', '+5511999997777', 'supplier@test.com',
    7200000, 15, 24, 'NEW', 'EXWORKS', 'Temos disponibilidade imediata. Entrega em 15 dias.', 'SUBMITTED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    ('resp-222-222-222-222-222222222222', 'eeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '44444444-4444-4444-4444-444444444444', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Ana Pereira', '+5511999996666', 'supplier2@test.com',
    7500000, 10, 36, 'NEW', 'DDF', 'Frete incluso. Entrega em 10 dias.', 'SUBMITTED', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
    
    -- Propostas para Evento 2 (Móveis)
    ('resp-333-333-333-333-333333333333', 'ffffffff-ffff-ffff-ffff-ffff-ffffffffffff', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Carlos Oliveira', '+5511999997777', 'supplier@test.com',
    2200000, 20, 12, 'NEW', 'FOB', 'Prazo de entrega 20 dias úteis.', 'SUBMITTED', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),

    -- Proposta aceita para Evento 5
    ('resp-444-444-444-444-444444444444', '33333333-3333-3333-3333-333333333330', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Carlos Oliveira', '+5511999997777', 'supplier@test.com',
    1450000, 7, 12, 'NEW', 'EXWORKS', 'Imediata', 'ACCEPTED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------
-- 5. Configurações de Sistema
-- -----------------------------------------------------------

INSERT INTO sys_config (key, value, description, created_at, updated_at)
VALUES 
    ('system.feature.chat.enabled', 'true', 'Enable real-time chat', NOW(), NOW()),
    ('system.feature.alerts.enabled', 'true', 'Enable alert system', NOW(), NOW()),
    ('system.feature.payment.enabled', 'false', 'Payment system (pending)', NOW(), NOW()),
    ('search.default_page_size', '20', 'Default page size for search', NOW(), NOW()),
    ('search.max_page_size', '100', 'Maximum page size for search', NOW(), NOW()),
    ('event.submission.deadline_hours', '168', 'Default submission deadline (7 days)', NOW(), NOW())
ON CONFLICT (key) DO NOTHING;

-- ----------------------------------------------------------
-- 6. MCC Categories (opcional)
-- -----------------------------------------------------------

-- O código MCC já está validado via frontend/schema, mas dados de exemplo
-- podem ser adicionados aqui se necessário

-- ----------------------------------------------------------
-- Resumo dos dados inseridos
-- -----------------------------------------------------------

SELECT 'Users: ' || COUNT(*)::text AS summary FROM usr_users
UNION ALL
SELECT 'Organizations: ' || COUNT(*)::text FROM usr_organizations
UNION ALL
SELECT 'Events: ' || COUNT(*)::text FROM src_sourcing_events
UNION ALL
SELECT 'Proposals: ' || COUNT(*)::text FROM src_supplier_responses;