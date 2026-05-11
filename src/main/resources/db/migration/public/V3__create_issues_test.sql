CREATE TABLE IF NOT EXISTS public.issues (
                                             id BIGSERIAL PRIMARY KEY,
                                             created_by BIGINT,
                                             apartment_id BIGINT,
                                             category_id BIGINT,
                                             title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
    );

INSERT INTO issues (id, title, description, status, priority)
VALUES (1, 'Test Issue', 'Water leaking from bathroom ceiling', 'OPEN', 'HIGH');