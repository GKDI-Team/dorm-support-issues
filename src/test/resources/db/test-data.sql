-- Deterministic integration-test data for issues-service.
-- This script runs after db/init.sql in the test profile only.
-- Clear dependent rows first so the category IDs below can be made deterministic
-- even when Spring recreates the application context during one test run.
TRUNCATE TABLE issue_status_history, issues;

-- init.sql creates the standard category rows with generated UUIDs.
-- Replace the IDs of the categories used by tests with stable test-only UUIDs.
INSERT INTO issue_categories (id, name, description)
VALUES
    ('11111111-1111-4111-8111-111111111111', 'Plumbing', 'Test category: plumbing'),
    ('22222222-2222-4222-8222-222222222222', 'Electricity', 'Test category: electricity'),
    ('33333333-3333-4333-8333-333333333333', 'Furniture', 'Test category: furniture')
ON CONFLICT (name) DO UPDATE
SET id = EXCLUDED.id,
    description = EXCLUDED.description;

-- User and block IDs are identifiers owned by other services; issues-service
-- deliberately does not maintain local user or block tables.
INSERT INTO issues (
    id, resident_id, block, room_number, title, description,
    category_id, status, location_details, created_at, updated_at
)
VALUES
    (
        'aaaaaaaa-0001-4000-8000-000000000001',
        'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
        'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
        '101', 'Leaking tap', 'The tap is leaking in the bathroom.',
        '11111111-1111-4111-8111-111111111111',
        'NEW', 'Bathroom', '2026-01-10 10:00:00+00', '2026-01-10 10:00:00+00'
    ),
    (
        'aaaaaaaa-0002-4000-8000-000000000002',
        'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
        'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
        '101', 'Broken light', 'The ceiling light does not work.',
        '22222222-2222-4222-8222-222222222222',
        'IN_PROGRESS', 'Bedroom', '2026-01-11 10:00:00+00', '2026-01-12 10:00:00+00'
    ),
    (
        'aaaaaaaa-0003-4000-8000-000000000003',
        'cccccccc-cccc-4ccc-8ccc-cccccccccccc',
        'dddddddd-dddd-4ddd-8ddd-dddddddddddd',
        '204', 'Damaged chair', 'One leg of the chair is broken.',
        '33333333-3333-4333-8333-333333333333',
        'CLOSED', 'Study area', '2026-01-09 10:00:00+00', '2026-01-13 10:00:00+00'
    )
ON CONFLICT (id) DO UPDATE SET
    resident_id = EXCLUDED.resident_id,
    block = EXCLUDED.block,
    room_number = EXCLUDED.room_number,
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    category_id = EXCLUDED.category_id,
    status = EXCLUDED.status,
    location_details = EXCLUDED.location_details,
    created_at = EXCLUDED.created_at,
    updated_at = EXCLUDED.updated_at;

INSERT INTO issue_status_history (
    id, issue_id, from_status, to_status, changed_by, changed_at
)
VALUES
    (
        'eeeeeeee-0001-4000-8000-000000000001',
        'aaaaaaaa-0001-4000-8000-000000000001',
        NULL, 'NEW', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
        '2026-01-10 10:00:00+00'
    ),
    (
        'eeeeeeee-0002-4000-8000-000000000002',
        'aaaaaaaa-0002-4000-8000-000000000002',
        NULL, 'NEW', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
        '2026-01-11 10:00:00+00'
    ),
    (
        'eeeeeeee-0003-4000-8000-000000000003',
        'aaaaaaaa-0002-4000-8000-000000000002',
        'NEW', 'IN_PROGRESS', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee',
        '2026-01-12 10:00:00+00'
    ),
    (
        'eeeeeeee-0004-4000-8000-000000000004',
        'aaaaaaaa-0003-4000-8000-000000000003',
        NULL, 'NEW', 'cccccccc-cccc-4ccc-8ccc-cccccccccccc',
        '2026-01-09 10:00:00+00'
    ),
    (
        'eeeeeeee-0005-4000-8000-000000000005',
        'aaaaaaaa-0003-4000-8000-000000000003',
        'NEW', 'IN_PROGRESS', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee',
        '2026-01-12 10:00:00+00'
    ),
    (
        'eeeeeeee-0006-4000-8000-000000000006',
        'aaaaaaaa-0003-4000-8000-000000000003',
        'IN_PROGRESS', 'CLOSED', 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee',
        '2026-01-13 10:00:00+00'
    )
ON CONFLICT (id) DO UPDATE SET
    issue_id = EXCLUDED.issue_id,
    from_status = EXCLUDED.from_status,
    to_status = EXCLUDED.to_status,
    changed_by = EXCLUDED.changed_by,
    changed_at = EXCLUDED.changed_at;
