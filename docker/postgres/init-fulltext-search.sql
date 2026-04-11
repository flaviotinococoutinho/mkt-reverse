-- Full-text search index for opportunities/sourcing events
-- PostgreSQL 16+ with tsvector full-text search

-- ============================================================
-- 1. Create search vector column and index
-- ============================================================

-- Add search vector column
ALTER TABLE src_sourcing_events 
ADD COLUMN IF NOT EXISTS search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('portuguese', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('portuguese', coalesce(description, '')), 'B') ||
    setweight(to_tsvector('portuguese', coalesce(product_name, '')), 'C') ||
    setweight(to_tsvector('portuguese', coalesce(product_description, '')), 'D')
) STORED;

-- Create GIN index for fast full-text search
CREATE INDEX IF NOT EXISTS idx_sourcing_events_search_vector 
ON src_sourcing_events USING GIN (search_vector);

-- ============================================================
-- 2. Create rank function
-- ============================================================

CREATE OR REPLACE FUNCTION search_rank_ relevance(p_query text, p_vector tsvector)
RETURNS float AS $$
BEGIN
    RETURN ts_rank(p_vector, to_tsquery('portuguese', p_query));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- ============================================================
-- 3. Search function with filters
-- ============================================================

CREATE OR REPLACE FUNCTION search_opportunities(
    p_tenant_id text,
    p_query text default null,
    p_mcc_category_code int default null,
    p_visibility text default 'PUBLIC',
    p_status text default 'PUBLISHED',
    p_sort_by text default 'PUBLISHED_AT',
    p_sort_dir text default 'DESC',
    p_page int default 0,
    p_size int default 20
)
RETURNS TABLE (
    id uuid,
    title text,
    description text,
    product_name text,
    status text,
    mcc_category_code int,
    visibility text,
    published_at timestamp,
    relevance float8,
    total_count bigint
) AS $$
DECLARE
    v_offset int := p_page * p_size;
    v_query_tsvector tsvector;
BEGIN
    -- Build query vector
    IF p_query IS NOT NULL AND length(p_query) > 0 THEN
        v_query_tsvector := to_tsvector('portuguese', p_query);
    END IF;

    RETURN QUERY
    SELECT 
        e.id,
        e.title,
        e.description,
        e.product_name,
        e.status,
        e.mcc_category_code,
        e.visibility,
        e.published_at,
        COALESCE(
            ts_rank(e.search_vector, v_query_tsvector)::float8,
            0.0
        ) as relevance,
        COUNT(*) OVER()::bigint as total_count
    FROM src_sourcing_events e
    WHERE e.tenant_id = p_tenant_id
      AND (p_visibility IS NULL OR p_visibility = 'ALL' OR e.visibility = p_visibility)
      AND (p_status IS NULL OR p_status = 'ALL' OR e.status = p_status)
      AND (p_mcc_category_code IS NULL OR e.mcc_category_code = p_mcc_category_code)
      AND (
        v_query_tsvector IS NULL 
        OR e.search_vector @@ v_query_tsvector
      )
    ORDER BY
        CASE 
            WHEN p_sort_by = 'RELEVANCE' AND p_query IS NOT NULL 
            THEN ts_rank(e.search_vector, v_query_tsvector)
        END DESC,
        CASE WHEN p_sort_dir = 'ASC' THEN e.published_at END ASC,
        CASE WHEN p_sort_dir = 'DESC' THEN e.published_at END DESC
    OFFSET v_offset
    LIMIT p_size;
END;
$$ LANGUAGE plpgsql STABLE;

-- ============================================================
-- 4. Autocomplete function (for typeahead)
-- ============================================================

CREATE OR REPLACE FUNCTION search_opportunities_autocomplete(
    p_tenant_id text,
    p_prefix text,
    p_limit int default 10
)
RETURNS TABLE (id uuid, title text, product_name text) AS $$
BEGIN
    RETURN QUERY
    SELECT e.id, e.title, e.product_name
    FROM src_sourcing_events e
    WHERE e.tenant_id = p_tenant_id
      AND e.status = 'PUBLISHED'
      AND e.visibility = 'PUBLIC'
      AND (
        e.title ILIKE p_prefix || '%'
        OR e.product_name ILIKE p_prefix || '%'
      )
    ORDER BY e.published_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql STABLE;

-- ============================================================
-- 5. Category aggregation (for facet filters)
-- ============================================================

CREATE OR REPLACE FUNCTION get_opportunity_category_facets(p_tenant_id text)
RETURNS TABLE (mcc_category_code int, count bigint, label text) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        e.mcc_category_code,
        COUNT(*)::bigint,
        CASE e.mcc_category_code
            WHEN 174 THEN 'Eletrônicos e Informática'
            WHEN 275 THEN 'Vestuário e Acessórios'
            WHEN 553 THEN 'Autos e Peças'
            WHEN 521 THEN 'Móveis e Decoração'
            WHEN 571 THEN 'Imóveis'
            WHEN 501 THEN 'Médicos e Farmacêuticos'
            WHEN 581 THEN 'Alimentos e Bebidas'
            WHEN 504 THEN 'Máquinas e Equipamentos'
            ELSE 'Outros'
        END::text as label
    FROM src_sourcing_events e
    WHERE e.tenant_id = p_tenant_id
      AND e.status = 'PUBLISHED'
      AND e.visibility = 'PUBLIC'
    GROUP BY e.mcc_category_code
    ORDER BY COUNT(*) DESC;
END;
$$ LANGUAGE plpgsql STABLE;

-- ============================================================
-- 6. Grant permissions
-- ============================================================

GRANT EXECUTE ON FUNCTION search_opportunities TO marketplace_user;
GRANT EXECUTE ON FUNCTION search_opportunities_autocomplete TO marketplace_user;
GRANT EXECUTE ON FUNCTION get_opportunity_category_facets TO marketplace_user;