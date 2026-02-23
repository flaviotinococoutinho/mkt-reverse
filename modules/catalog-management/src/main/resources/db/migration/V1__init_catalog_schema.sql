CREATE TABLE taxonomy_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    parent_id BIGINT REFERENCES taxonomy_categories(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE asset_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    parent_id BIGINT REFERENCES asset_types(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE attribute_definitions (
    id BIGSERIAL PRIMARY KEY,
    asset_type_id BIGINT NOT NULL REFERENCES asset_types(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    is_searchable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (asset_type_id, name)
);

CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    tag VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    asset_type_id BIGINT NOT NULL REFERENCES asset_types(id),
    taxonomy_category_id BIGINT REFERENCES taxonomy_categories(id),
    attributes JSONB,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Recommended index for JSONB queries
CREATE INDEX idx_assets_attributes ON assets USING GIN (attributes);

CREATE TABLE asset_relationships (
    id BIGSERIAL PRIMARY KEY,
    source_asset_id BIGINT NOT NULL REFERENCES assets(id),
    target_asset_id BIGINT NOT NULL REFERENCES assets(id),
    relationship_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
