PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS app_metadata (
    metadata_key TEXT PRIMARY KEY,
    metadata_value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS beneficiaries (
    id TEXT PRIMARY KEY,
    beneficiary_type TEXT NOT NULL CHECK (beneficiary_type IN ('Household', 'Individual')),
    full_name TEXT NOT NULL,
    monthly_income NUMERIC NOT NULL CHECK (monthly_income >= 0),
    dependants INTEGER NOT NULL CHECK (dependants BETWEEN 0 AND 20),
    employment_status TEXT NOT NULL CHECK (employment_status IN ('Employed', 'Part-time', 'Unemployed')),
    location TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS aid_distributions (
    id TEXT PRIMARY KEY,
    aid_type TEXT NOT NULL CHECK (aid_type IN ('Cash', 'Food')),
    beneficiary_id TEXT NOT NULL,
    aid_value NUMERIC NOT NULL CHECK (aid_value > 0),
    package_description TEXT NOT NULL DEFAULT '',
    distributed_on TEXT NOT NULL,
    distribution_status TEXT NOT NULL CHECK (distribution_status IN ('Scheduled', 'Distributed')),
    CONSTRAINT fk_aid_beneficiary
        FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_daily_aid
        UNIQUE (beneficiary_id, aid_type, distributed_on)
);

CREATE INDEX IF NOT EXISTS idx_aid_beneficiary
    ON aid_distributions (beneficiary_id);

CREATE INDEX IF NOT EXISTS idx_aid_date
    ON aid_distributions (distributed_on);
