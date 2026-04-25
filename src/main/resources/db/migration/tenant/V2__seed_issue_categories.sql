INSERT INTO issue_categories (name, description) VALUES
    ('Electrical', 'Power issues, wiring, lights, breaker trips'),
    ('Plumbing', 'Leaks, clogged drains, pipe issues, low water pressure'),
    ('Elevator', 'Elevator stuck, noise, malfunction, door issues'),
    ('HVAC', 'Heating, cooling, ventilation, AC issues'),
    ('Security', 'Locks, cameras, access control, unauthorized entry'),
    ('Emergency', 'Fire, gas leak, flooding, or immediate danger'),
    ('Cleaning', 'Trash overflow, dirty common areas, hygiene issues'),
    ('Structural', 'Cracks, ceiling damage, stairs, walls, building structure')
ON CONFLICT (name) DO NOTHING;