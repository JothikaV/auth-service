INSERT INTO roles (id, name, created_by, created_date, modified_by, modified_date)
VALUES (1, 'ROLE_USER', 'system', NOW(),'system', NOW());

INSERT INTO roles (id, name, created_by, created_date, modified_by, modified_date)
VALUES (2, 'ROLE_ADMIN', 'system', NOW(),'system', NOW());

INSERT INTO users (id, username, email, password, last_login, created_by, created_date, modified_by, modified_date)
VALUES (
    1,
    'admin',
    'admin@system.com',
    '$2a$10$dvh4OPvKBCssmamEOvzSXOoT63OCWftRgzuMRkN0I5uywlzdbwpyK',
    NOW(),
    'system',
    NOW(),
    'system',
    NOW()
);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 2);
