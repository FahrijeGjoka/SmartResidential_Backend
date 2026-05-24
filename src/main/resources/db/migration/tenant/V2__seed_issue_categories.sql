INSERT INTO issue_categories (name, description, required_specialization) VALUES
    ('Electrical', 'Power issues, wiring, lights, breaker trips', 'Electrical'),
    ('Plumbing', 'Leaks, clogged drains, pipe issues, low water pressure', 'Plumbing'),
    ('Elevator', 'Elevator stuck, noise, malfunction, door issues', 'Elevator'),
    ('HVAC', 'Heating, cooling, ventilation, AC issues', 'HVAC'),
    ('Security', 'Locks, cameras, access control, unauthorized entry', 'Security'),
    ('Emergency', 'Fire, gas leak, flooding, or immediate danger', 'General maintenance'),
    ('Cleaning', 'Trash overflow, dirty common areas, hygiene issues', 'Cleaning'),
    ('Structural', 'Cracks, ceiling damage, stairs, walls, building structure', 'Structural'),
    ('General maintenance', 'General maintenance and issues that need manual triage', 'General maintenance')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    required_specialization = EXCLUDED.required_specialization;
