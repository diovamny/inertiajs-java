CREATE INDEX idx_users_account ON users (account_id);
CREATE INDEX idx_organizations_account ON organizations (account_id);
CREATE INDEX idx_contacts_account ON contacts (account_id);
CREATE INDEX idx_contacts_organization ON contacts (organization_id);
