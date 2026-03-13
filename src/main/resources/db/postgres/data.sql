INSERT INTO vets (first_name, last_name) SELECT 'James', 'Carter' WHERE NOT EXISTS (SELECT * FROM vets WHERE id=1);
INSERT INTO vets (first_name, last_name) SELECT 'Helen', 'Leary' WHERE NOT EXISTS (SELECT * FROM vets WHERE id=2);
INSERT INTO vets (first_name, last_name) SELECT 'Linda', 'Douglas' WHERE NOT EXISTS (SELECT * FROM vets WHERE id=3);
INSERT INTO vets (first_name, last_name) SELECT 'Rafael', 'Ortega' WHERE NOT EXISTS (SELECT * FROM vets WHERE id=4);
INSERT INTO vets (first_name, last_name) SELECT 'Henry', 'Stevens' WHERE NOT EXISTS (SELECT * FROM vets WHERE id=5);
INSERT INTO vets (first_name, last_name) SELECT 'Sharon', 'Jenkins' WHERE NOT EXISTS (SELECT * FROM vets WHERE id=6);

INSERT INTO specialties (name) SELECT 'radiology' WHERE NOT EXISTS (SELECT * FROM specialties WHERE name='radiology');
INSERT INTO specialties (name) SELECT 'surgery' WHERE NOT EXISTS (SELECT * FROM specialties WHERE name='surgery');
INSERT INTO specialties (name) SELECT 'dentistry' WHERE NOT EXISTS (SELECT * FROM specialties WHERE name='dentistry');

INSERT INTO vet_specialties VALUES (2, 1) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (3, 2) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (3, 3) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (4, 2) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (5, 1) ON CONFLICT (vet_id, specialty_id) DO NOTHING;

INSERT INTO types (name) SELECT 'cat' WHERE NOT EXISTS (SELECT * FROM types WHERE name='cat');
INSERT INTO types (name) SELECT 'dog' WHERE NOT EXISTS (SELECT * FROM types WHERE name='dog');
INSERT INTO types (name) SELECT 'lizard' WHERE NOT EXISTS (SELECT * FROM types WHERE name='lizard');
INSERT INTO types (name) SELECT 'snake' WHERE NOT EXISTS (SELECT * FROM types WHERE name='snake');
INSERT INTO types (name) SELECT 'bird' WHERE NOT EXISTS (SELECT * FROM types WHERE name='bird');
INSERT INTO types (name) SELECT 'hamster' WHERE NOT EXISTS (SELECT * FROM types WHERE name='hamster');

INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=1);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=2);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=3);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=4);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=5);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=6);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=7);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=8);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=9);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=10);

INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Leo', '2000-09-07', 1, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=1);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Basil', '2002-08-06', 6, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=2);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Rosy', '2001-04-17', 2, 3 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=3);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Jewel', '2000-03-07', 2, 3 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=4);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Iggy', '2000-11-30', 3, 4 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=5);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'George', '2000-01-20', 4, 5 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=6);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Samantha', '1995-09-04', 1, 6 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=7);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Max', '1995-09-04', 1, 6 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=8);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Lucky', '1999-08-06', 5, 7 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=9);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Mulligan', '1997-02-24', 2, 8 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=10);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Freddy', '2000-03-09', 5, 9 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=11);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Lucky', '2000-06-24', 2, 10 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=12);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Sly', '2002-06-08', 1, 10 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=13);

INSERT INTO visits (pet_id, visit_date, description) SELECT 7, '2010-03-04', 'rabies shot' WHERE NOT EXISTS (SELECT * FROM visits WHERE id=1);
INSERT INTO visits (pet_id, visit_date, description) SELECT 8, '2011-03-04', 'rabies shot' WHERE NOT EXISTS (SELECT * FROM visits WHERE id=2);
INSERT INTO visits (pet_id, visit_date, description) SELECT 8, '2009-06-04', 'neutered' WHERE NOT EXISTS (SELECT * FROM visits WHERE id=3);
INSERT INTO visits (pet_id, visit_date, description) SELECT 7, '2008-09-04', 'spayed' WHERE NOT EXISTS (SELECT * FROM visits WHERE id=4);

INSERT INTO appointment_types (name, default_duration_minutes, specialty_id, description, version) SELECT 'Checkup', 30, NULL, 'General wellness exam', 0 WHERE NOT EXISTS (SELECT * FROM appointment_types WHERE name='Checkup');
INSERT INTO appointment_types (name, default_duration_minutes, specialty_id, description, version) SELECT 'Vaccination', 30, NULL, 'Standard vaccination visit', 0 WHERE NOT EXISTS (SELECT * FROM appointment_types WHERE name='Vaccination');
INSERT INTO appointment_types (name, default_duration_minutes, specialty_id, description, version) SELECT 'Surgery', 90, 2, 'Surgical procedure', 0 WHERE NOT EXISTS (SELECT * FROM appointment_types WHERE name='Surgery');
INSERT INTO appointment_types (name, default_duration_minutes, specialty_id, description, version) SELECT 'Dental Cleaning', 60, 3, 'Professional dental cleaning', 0 WHERE NOT EXISTS (SELECT * FROM appointment_types WHERE name='Dental Cleaning');
INSERT INTO appointment_types (name, default_duration_minutes, specialty_id, description, version) SELECT 'X-Ray', 30, 1, 'Diagnostic imaging', 0 WHERE NOT EXISTS (SELECT * FROM appointment_types WHERE name='X-Ray');
INSERT INTO appointment_types (name, default_duration_minutes, specialty_id, description, version) SELECT 'Emergency', 60, NULL, 'Emergency visit', 0 WHERE NOT EXISTS (SELECT * FROM appointment_types WHERE name='Emergency');

INSERT INTO clinic_schedule_config (day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) SELECT 1, '09:00:00', '17:00:00', 30, TRUE, 0 WHERE NOT EXISTS (SELECT * FROM clinic_schedule_config WHERE day_of_week=1);
INSERT INTO clinic_schedule_config (day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) SELECT 2, '09:00:00', '17:00:00', 30, TRUE, 0 WHERE NOT EXISTS (SELECT * FROM clinic_schedule_config WHERE day_of_week=2);
INSERT INTO clinic_schedule_config (day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) SELECT 3, '09:00:00', '17:00:00', 30, TRUE, 0 WHERE NOT EXISTS (SELECT * FROM clinic_schedule_config WHERE day_of_week=3);
INSERT INTO clinic_schedule_config (day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) SELECT 4, '09:00:00', '17:00:00', 30, TRUE, 0 WHERE NOT EXISTS (SELECT * FROM clinic_schedule_config WHERE day_of_week=4);
INSERT INTO clinic_schedule_config (day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) SELECT 5, '09:00:00', '17:00:00', 30, TRUE, 0 WHERE NOT EXISTS (SELECT * FROM clinic_schedule_config WHERE day_of_week=5);
INSERT INTO clinic_schedule_config (day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) SELECT 6, '09:00:00', '13:00:00', 30, TRUE, 0 WHERE NOT EXISTS (SELECT * FROM clinic_schedule_config WHERE day_of_week=6);
INSERT INTO clinic_schedule_config (day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) SELECT 7, '09:00:00', '13:00:00', 30, FALSE, 0 WHERE NOT EXISTS (SELECT * FROM clinic_schedule_config WHERE day_of_week=7);

INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 1, 1, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=1 AND day_of_week=1);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 1, 2, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=1 AND day_of_week=2);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 1, 3, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=1 AND day_of_week=3);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 1, 4, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=1 AND day_of_week=4);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 1, 5, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=1 AND day_of_week=5);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 1, 6, '09:00:00', '13:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=1 AND day_of_week=6);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 2, 1, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=2 AND day_of_week=1);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 2, 2, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=2 AND day_of_week=2);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 2, 3, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=2 AND day_of_week=3);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 2, 4, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=2 AND day_of_week=4);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 2, 5, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=2 AND day_of_week=5);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 3, 1, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=3 AND day_of_week=1);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 3, 2, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=3 AND day_of_week=2);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 3, 3, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=3 AND day_of_week=3);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 3, 4, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=3 AND day_of_week=4);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 3, 5, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=3 AND day_of_week=5);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 3, 6, '09:00:00', '13:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=3 AND day_of_week=6);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 4, 1, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=4 AND day_of_week=1);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 4, 2, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=4 AND day_of_week=2);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 4, 3, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=4 AND day_of_week=3);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 4, 4, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=4 AND day_of_week=4);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 4, 5, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=4 AND day_of_week=5);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 5, 1, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=5 AND day_of_week=1);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 5, 2, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=5 AND day_of_week=2);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 5, 3, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=5 AND day_of_week=3);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 5, 4, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=5 AND day_of_week=4);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 5, 5, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=5 AND day_of_week=5);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 6, 1, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=6 AND day_of_week=1);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 6, 2, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=6 AND day_of_week=2);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 6, 3, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=6 AND day_of_week=3);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 6, 4, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=6 AND day_of_week=4);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 6, 5, '09:00:00', '17:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=6 AND day_of_week=5);
INSERT INTO vet_schedules (vet_id, day_of_week, start_time, end_time, is_available, version) SELECT 6, 6, '09:00:00', '13:00:00', TRUE, 0 WHERE NOT EXISTS (SELECT * FROM vet_schedules WHERE vet_id=6 AND day_of_week=6);

INSERT INTO vet_time_off (vet_id, off_date, reason, version) SELECT 2, '2026-04-06', 'Conference', 0 WHERE NOT EXISTS (SELECT * FROM vet_time_off WHERE vet_id=2 AND off_date='2026-04-06');
INSERT INTO vet_time_off (vet_id, off_date, reason, version) SELECT 4, '2026-04-07', 'Vacation day', 0 WHERE NOT EXISTS (SELECT * FROM vet_time_off WHERE vet_id=4 AND off_date='2026-04-07');

INSERT INTO appointments (appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, created_at, version) SELECT '2026-04-06', '09:00:00', '09:30:00', 'SCHEDULED', 1, 1, 1, CURRENT_TIMESTAMP, 0 WHERE NOT EXISTS (SELECT * FROM appointments WHERE id=1);
INSERT INTO appointments (appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, created_at, version) SELECT '2026-04-06', '10:00:00', '10:30:00', 'CONFIRMED', 7, 1, 2, CURRENT_TIMESTAMP, 0 WHERE NOT EXISTS (SELECT * FROM appointments WHERE id=2);
INSERT INTO appointments (appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, created_at, version) SELECT '2026-04-07', '09:00:00', '10:30:00', 'SCHEDULED', 3, 3, 3, CURRENT_TIMESTAMP, 0 WHERE NOT EXISTS (SELECT * FROM appointments WHERE id=3);
INSERT INTO appointments (appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, created_at, version) SELECT '2026-04-07', '14:00:00', '14:30:00', 'SCHEDULED', 5, 1, 1, CURRENT_TIMESTAMP, 0 WHERE NOT EXISTS (SELECT * FROM appointments WHERE id=4);

SELECT setval(pg_get_serial_sequence('vets', 'id'), COALESCE((SELECT MAX(id) FROM vets), 0));
SELECT setval(pg_get_serial_sequence('specialties', 'id'), COALESCE((SELECT MAX(id) FROM specialties), 0));
SELECT setval(pg_get_serial_sequence('types', 'id'), COALESCE((SELECT MAX(id) FROM types), 0));
SELECT setval(pg_get_serial_sequence('owners', 'id'), COALESCE((SELECT MAX(id) FROM owners), 0));
SELECT setval(pg_get_serial_sequence('pets', 'id'), COALESCE((SELECT MAX(id) FROM pets), 0));
SELECT setval(pg_get_serial_sequence('visits', 'id'), COALESCE((SELECT MAX(id) FROM visits), 0));
SELECT setval(pg_get_serial_sequence('appointment_types', 'id'), COALESCE((SELECT MAX(id) FROM appointment_types), 0));
SELECT setval(pg_get_serial_sequence('appointments', 'id'), COALESCE((SELECT MAX(id) FROM appointments), 0));
SELECT setval(pg_get_serial_sequence('clinic_schedule_config', 'id'), COALESCE((SELECT MAX(id) FROM clinic_schedule_config), 0));
SELECT setval(pg_get_serial_sequence('vet_schedules', 'id'), COALESCE((SELECT MAX(id) FROM vet_schedules), 0));
SELECT setval(pg_get_serial_sequence('vet_time_off', 'id'), COALESCE((SELECT MAX(id) FROM vet_time_off), 0));
