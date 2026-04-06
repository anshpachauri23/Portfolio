-- V2: Seed departments, request types, and demo users
-- Passwords are bcrypt hashes of "Password1!" (cost 12)

INSERT INTO departments (name, code) VALUES
    ('Engineering', 'ENG'),
    ('Operations',  'OPS'),
    ('HR',          'HR');

INSERT INTO request_types (name, description, approval_required, active) VALUES
    ('New Device',      'Request for a new laptop, desktop, or peripheral',            TRUE,  TRUE),
    ('Device Replacement', 'Replace a faulty or end-of-life device',                   FALSE, TRUE),
    ('Repair',          'Submit an asset for repair',                                   FALSE, TRUE),
    ('Software Access', 'Request access to a licensed software application',            TRUE,  TRUE),
    ('Return Asset',    'Return an assigned asset back to IT',                          FALSE, TRUE);

-- Demo users (one per role).  Passwords: Password1!
-- bcrypt hash below is cost-12 for "Password1!"
INSERT INTO users (name, email, employee_code, password_hash, role, department_id) VALUES
    ('Alice Admin',     'alice@company.com',   'EMP001',
     '$2a$12$FVBn.LhS/kv/1x/FBsN9C.TKsMRqfmHxBioNnOzF1lpJFe3uB2aSe', 'ADMIN',      1),
    ('Mike Manager',    'mike@company.com',    'EMP002',
     '$2a$12$FVBn.LhS/kv/1x/FBsN9C.TKsMRqfmHxBioNnOzF1lpJFe3uB2aSe', 'MANAGER',    1),
    ('Tina Tech',       'tina@company.com',    'EMP003',
     '$2a$12$FVBn.LhS/kv/1x/FBsN9C.TKsMRqfmHxBioNnOzF1lpJFe3uB2aSe', 'TECHNICIAN', 2),
    ('Emma Employee',   'emma@company.com',    'EMP004',
     '$2a$12$FVBn.LhS/kv/1x/FBsN9C.TKsMRqfmHxBioNnOzF1lpJFe3uB2aSe', 'EMPLOYEE',   3);

-- Give Emma a manager
UPDATE users SET manager_id = (SELECT id FROM users WHERE email = 'mike@company.com')
WHERE email = 'emma@company.com';
