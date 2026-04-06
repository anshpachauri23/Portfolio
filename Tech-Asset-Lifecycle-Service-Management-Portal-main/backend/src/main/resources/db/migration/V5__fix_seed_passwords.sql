-- V5: Fix seed user password hashes.
-- The hash in V2 was incorrect. This migration updates all seed users
-- to the correct bcrypt (cost-12) hash for "Password1!".

UPDATE users
SET password_hash = '$2a$12$SUrD1P0raoYBE3slnPE4F.8MgPSIORFZeyW647zLEcTdNbfNnxkCi'
WHERE email IN (
    'alice@company.com',
    'mike@company.com',
    'tina@company.com',
    'emma@company.com'
);
