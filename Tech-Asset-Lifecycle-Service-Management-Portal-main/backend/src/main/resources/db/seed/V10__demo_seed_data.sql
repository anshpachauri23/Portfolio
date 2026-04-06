-- V10__demo_seed_data.sql
-- Run only in profiles that include classpath:db/seed explicitly

-- Add 10 demo assets
INSERT INTO assets (asset_tag, serial_number, asset_type, vendor, model, status, purchase_date, warranty_expiry, cost_center) VALUES 
('TAG-DELL-1X', 'SN-D12345', 'LAPTOP', 'Dell', 'XPS 15', 'AVAILABLE', '2023-01-10', '2026-01-10', 'IT-101'),
('TAG-MAC-2Y', 'SN-M55432', 'LAPTOP', 'Apple', 'MacBook Pro M2', 'AVAILABLE', '2023-04-15', '2026-04-15', 'IT-101'),
('TAG-MON-3A', 'SN-X98711', 'MONITOR', 'LG', 'UltraFine 27', 'AVAILABLE', '2022-11-01', '2024-11-01', 'FAC-201'),
('TAG-MON-4B', 'SN-X98712', 'MONITOR', 'LG', 'UltraFine 27', 'AVAILABLE', '2022-11-01', '2024-11-01', 'FAC-201'),
('TAG-MOB-5C', 'SN-P22233', 'MOBILE_DEVICE', 'Samsung', 'Galaxy S23', 'AVAILABLE', '2023-08-20', '2025-08-20', 'SALES-301'),
('TAG-MOB-6D', 'SN-I44455', 'MOBILE_DEVICE', 'Apple', 'iPhone 15', 'AVAILABLE', '2023-09-25', '2025-09-25', 'EXEC-401'),
('TAG-ACC-7E', 'SN-K11100', 'ACCESSORY', 'Logitech', 'MX Master 3', 'AVAILABLE', '2023-05-10', '2024-05-10', 'IT-101'),
('TAG-ACC-8F', 'SN-K11101', 'ACCESSORY', 'Keychron', 'K8 Pro', 'AVAILABLE', '2023-05-10', '2024-05-10', 'IT-101'),
('TAG-SVR-9G', 'SN-H88899', 'SERVER', 'Dell', 'PowerEdge R740', 'AVAILABLE', '2021-02-15', '2026-02-15', 'OPS-501'),
('TAG-SVR-0H', 'SN-H88890', 'SERVER', 'HP', 'ProLiant DL380', 'AVAILABLE', '2021-02-15', '2026-02-15', 'OPS-501');

-- Add 8 service requests assigned arbitrarily to users
INSERT INTO service_requests (request_type_id, title, description, priority, status, approval_required, created_at, updated_at) VALUES 
(1, 'Need a new laptop', 'My current laptop is extremely slow.', 'HIGH', 'SUBMITTED', true, NOW() - INTERVAL '5 DAYS', NOW()),
(1, 'Laptop for new hire', 'New developer joining next week.', 'MEDIUM', 'PENDING_APPROVAL', true, NOW() - INTERVAL '4 DAYS', NOW()),
(3, 'Broken monitor', 'The screen keeps flickering.', 'MEDIUM', 'IN_PROGRESS', false, NOW() - INTERVAL '3 DAYS', NOW()),
(4, 'Access to GitHub', 'Need access to the org repo.', 'LOW', 'COMPLETED', true, NOW() - INTERVAL '10 DAYS', NOW()),
(5, 'Returning old phone', 'Got a new one, returning the old.', 'LOW', 'CLOSED', false, NOW() - INTERVAL '20 DAYS', NOW() - INTERVAL '15 DAYS'),
(2, 'Docking station dead', 'Need a replacement Lenovo dock.', 'HIGH', 'SUBMITTED', false, NOW() - INTERVAL '2 DAYS', NOW()),
(4, 'Jira License', 'Adding to project alpha.', 'MEDIUM', 'APPROVED', true, NOW() - INTERVAL '1 DAY', NOW()),
(2, 'Lost mouse', 'Forgot my mouse at home.', 'LOW', 'REJECTED', false, NOW() - INTERVAL '1 DAY', NOW());
