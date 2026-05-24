INSERT INTO issue_categories (name, description, required_specialization)
VALUES ('General maintenance', 'General maintenance and issues that need manual triage', 'General maintenance')
ON CONFLICT (name) DO UPDATE
SET required_specialization = COALESCE(issue_categories.required_specialization, EXCLUDED.required_specialization),
    description = COALESCE(issue_categories.description, EXCLUDED.description);