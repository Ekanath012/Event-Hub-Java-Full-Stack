CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_categories_name UNIQUE (name)
);

CREATE TABLE venues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_venues_name (name),
    INDEX idx_venues_city (city)
);

CREATE TABLE events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    banner_image VARCHAR(500),
    category_id BIGINT NOT NULL,
    organizer_id BIGINT NOT NULL,
    venue_id BIGINT NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    registration_start DATE NOT NULL,
    registration_deadline DATE NOT NULL,
    max_attendees INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    rules TEXT,
    contact_email VARCHAR(190),
    contact_phone VARCHAR(30),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES users(id),
    CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues(id),
    INDEX idx_events_title (title),
    INDEX idx_events_date (event_date),
    INDEX idx_events_category (category_id),
    INDEX idx_events_status (status),
    INDEX idx_events_organizer (organizer_id)
);

INSERT INTO categories (name, description) VALUES
('Technology', 'Technology and software events'),
('Hackathon', 'Competitive software and innovation events'),
('Workshop', 'Hands-on learning workshops'),
('Seminar', 'Focused educational seminars'),
('Conference', 'Large-scale professional conferences'),
('Cultural', 'Cultural and arts events'),
('Sports', 'Sports and fitness events'),
('Competition', 'Academic and professional competitions'),
('Placement', 'Career and placement events'),
('Other', 'Other event types');


INSERT INTO venues (name, address, city, state, postal_code) VALUES
('Main Auditorium', 'EventHub Campus, Main Road', 'Belagavi', 'Karnataka', '590001'),
('Innovation Lab', 'Technology Park, University Road', 'Belagavi', 'Karnataka', '590018'),
('City Convention Hall', 'Central Business District', 'Belagavi', 'Karnataka', '590002');
