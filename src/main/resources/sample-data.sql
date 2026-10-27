INSERT INTO beneficiaries (
    id, beneficiary_type, full_name, monthly_income, dependants, employment_status, location
) VALUES
    ('BEN-001', 'Household', 'Siti Aminah', 1200.00, 4, 'Part-time', 'Subang Jaya'),
    ('BEN-002', 'Individual', 'Ravi Kumar', 900.00, 0, 'Unemployed', 'Petaling Jaya'),
    ('BEN-003', 'Household', 'Lim Family', 2800.00, 2, 'Employed', 'Puchong');

INSERT INTO aid_distributions (
    id, aid_type, beneficiary_id, aid_value, package_description, distributed_on, distribution_status
) VALUES
    ('AID-001', 'Cash', 'BEN-001', 250.00, '', '2026-07-18', 'Distributed'),
    ('AID-002', 'Food', 'BEN-002', 120.00, 'Essential groceries', '2026-07-19', 'Distributed');
