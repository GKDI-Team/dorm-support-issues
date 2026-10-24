CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS issue_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(80) NOT NULL,
    description TEXT,
    CONSTRAINT uk_issue_categories_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS issues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resident_id UUID NOT NULL,
    block_id UUID NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    location_details VARCHAR(250) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issues_category
        FOREIGN KEY (category_id) REFERENCES issue_categories (id),
    CONSTRAINT ck_issues_status
        CHECK (status IN ('NEW', 'IN_PROGRESS', 'CLOSED'))
);

CREATE TABLE IF NOT EXISTS issue_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issue_status_history_issue
        FOREIGN KEY (issue_id) REFERENCES issues (id),
    CONSTRAINT ck_issue_status_history_from_status
        CHECK (from_status IS NULL OR from_status IN ('NEW', 'IN_PROGRESS', 'CLOSED')),
    CONSTRAINT ck_issue_status_history_to_status
        CHECK (to_status IN ('NEW', 'IN_PROGRESS', 'CLOSED'))
);

CREATE INDEX IF NOT EXISTS ix_issues_resident_created_at
    ON issues (resident_id, created_at DESC, id);

CREATE INDEX IF NOT EXISTS ix_issues_status_created_at
    ON issues (status, created_at DESC, id);

CREATE INDEX IF NOT EXISTS ix_issue_status_history_issue_changed_at
    ON issue_status_history (issue_id, changed_at);

INSERT INTO issue_categories (name, description)
VALUES
    ('Plumbing', 'Problems with taps, pipes and water supply'),
    ('Electricity', 'Problems with lamps, sockets and wiring'),
    ('Furniture', 'Broken or damaged furniture'),
    ('Heating', 'Problems with heating or radiators'),
    ('Other', 'Other dormitory problems')
ON CONFLICT (name) DO NOTHING;
