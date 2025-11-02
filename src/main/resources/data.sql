-- ==================== CREAR ROLES ====================
INSERT INTO role (name) VALUES ('ROLE_OWNER');
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
INSERT INTO role (name) VALUES ('ROLE_USER');

-- ==================== CREAR ORGANIZACIONES ====================
INSERT INTO organization (name, description, tax_id, address, phone, email, active) 
VALUES ('Parkings del Centro', 'Organización de parqueaderos del centro de la ciudad', '123456789-0', 'Calle Principal 123', '+57 300 1234567', 'info@parkingcentro.com', true);

INSERT INTO organization (name, description, tax_id, address, phone, email, active) 
VALUES ('Parkings Norte', 'Organización de parqueaderos zona norte', '987654321-0', 'Avenida Norte 456', '+57 300 9876543', 'info@parkingnorte.com', true);

-- ==================== CREAR USUARIOS ====================
INSERT INTO "user" (username, password, active, organization_id) 
VALUES ('owner1', '1234', true, 1);

INSERT INTO "user" (username, password, active, organization_id) 
VALUES ('admin1', '1234', true, 1);

INSERT INTO "user" (username, password, active, organization_id) 
VALUES ('user1', '1234', true, 1);

INSERT INTO "user" (username, password, active, organization_id) 
VALUES ('owner2', '1234', true, 2);

-- ==================== ASIGNAR ROLES A USUARIOS ====================
INSERT INTO user_roles (user_id, roles_id) SELECT u.id, r.id FROM "user" u, role r WHERE u.username = 'owner1' AND r.name = 'ROLE_OWNER';
INSERT INTO user_roles (user_id, roles_id) SELECT u.id, r.id FROM "user" u, role r WHERE u.username = 'admin1' AND r.name = 'ROLE_ADMIN';
INSERT INTO user_roles (user_id, roles_id) SELECT u.id, r.id FROM "user" u, role r WHERE u.username = 'user1' AND r.name = 'ROLE_USER';
INSERT INTO user_roles (user_id, roles_id) SELECT u.id, r.id FROM "user" u, role r WHERE u.username = 'owner2' AND r.name = 'ROLE_OWNER';