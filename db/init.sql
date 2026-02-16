-- Tabla blueprints
CREATE TABLE IF NOT EXISTS blueprints (
    id SERIAL PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    UNIQUE (author, name)
);

-- Tabla blueprint_points (para @ElementCollection de JPA)
CREATE TABLE IF NOT EXISTS blueprint_points (
    blueprint_id BIGINT NOT NULL REFERENCES blueprints(id) ON DELETE CASCADE,
    x INT NOT NULL,
    y INT NOT NULL,
    point_order INT NOT NULL,
    PRIMARY KEY (blueprint_id, point_order)
);

-- Blueprints
INSERT INTO blueprints (author, name) VALUES
('john', 'house'),
('john', 'garage'),
('jane', 'garden');

-- Points: house (blueprint_id=1)
INSERT INTO blueprint_points (blueprint_id, x, y, point_order) VALUES
(1, 0, 0, 0),
(1, 10, 0, 1),
(1, 10, 10, 2),
(1, 0, 10, 3);

-- Points: garage (blueprint_id=2)
INSERT INTO blueprint_points (blueprint_id, x, y, point_order) VALUES
(2, 5, 5, 0),
(2, 15, 5, 1),
(2, 15, 15, 2);

-- Points: garden (blueprint_id=3)
INSERT INTO blueprint_points (blueprint_id, x, y, point_order) VALUES
(3, 2, 2, 0),
(3, 3, 4, 1),
(3, 6, 7, 2);
