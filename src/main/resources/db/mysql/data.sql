INSERT IGNORE INTO vets VALUES (1, 'James', 'Carter');
INSERT IGNORE INTO vets VALUES (2, 'Helen', 'Leary');
INSERT IGNORE INTO vets VALUES (3, 'Linda', 'Douglas');
INSERT IGNORE INTO vets VALUES (4, 'Rafael', 'Ortega');
INSERT IGNORE INTO vets VALUES (5, 'Henry', 'Stevens');
INSERT IGNORE INTO vets VALUES (6, 'Sharon', 'Jenkins');

INSERT IGNORE INTO specialties VALUES (1, 'radiology');
INSERT IGNORE INTO specialties VALUES (2, 'surgery');
INSERT IGNORE INTO specialties VALUES (3, 'dentistry');

INSERT IGNORE INTO vet_specialties VALUES (2, 1);
INSERT IGNORE INTO vet_specialties VALUES (3, 2);
INSERT IGNORE INTO vet_specialties VALUES (3, 3);
INSERT IGNORE INTO vet_specialties VALUES (4, 2);
INSERT IGNORE INTO vet_specialties VALUES (5, 1);

INSERT IGNORE INTO types VALUES (1, 'cat');
INSERT IGNORE INTO types VALUES (2, 'dog');
INSERT IGNORE INTO types VALUES (3, 'lizard');
INSERT IGNORE INTO types VALUES (4, 'snake');
INSERT IGNORE INTO types VALUES (5, 'bird');
INSERT IGNORE INTO types VALUES (6, 'hamster');

INSERT IGNORE INTO owners VALUES (1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT IGNORE INTO owners VALUES (2, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT IGNORE INTO owners VALUES (3, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT IGNORE INTO owners VALUES (4, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198');
INSERT IGNORE INTO owners VALUES (5, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765');
INSERT IGNORE INTO owners VALUES (6, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT IGNORE INTO owners VALUES (7, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387');
INSERT IGNORE INTO owners VALUES (8, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683');
INSERT IGNORE INTO owners VALUES (9, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435');
INSERT IGNORE INTO owners VALUES (10, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

INSERT IGNORE INTO pets VALUES (1, 'Leo', '2000-09-07', 1, 1);
INSERT IGNORE INTO pets VALUES (2, 'Basil', '2002-08-06', 6, 2);
INSERT IGNORE INTO pets VALUES (3, 'Rosy', '2001-04-17', 2, 3);
INSERT IGNORE INTO pets VALUES (4, 'Jewel', '2000-03-07', 2, 3);
INSERT IGNORE INTO pets VALUES (5, 'Iggy', '2000-11-30', 3, 4);
INSERT IGNORE INTO pets VALUES (6, 'George', '2000-01-20', 4, 5);
INSERT IGNORE INTO pets VALUES (7, 'Samantha', '1995-09-04', 1, 6);
INSERT IGNORE INTO pets VALUES (8, 'Max', '1995-09-04', 1, 6);
INSERT IGNORE INTO pets VALUES (9, 'Lucky', '1999-08-06', 5, 7);
INSERT IGNORE INTO pets VALUES (10, 'Mulligan', '1997-02-24', 2, 8);
INSERT IGNORE INTO pets VALUES (11, 'Freddy', '2000-03-09', 5, 9);
INSERT IGNORE INTO pets VALUES (12, 'Lucky', '2000-06-24', 2, 10);
INSERT IGNORE INTO pets VALUES (13, 'Sly', '2002-06-08', 1, 10);

INSERT IGNORE INTO visits VALUES (1, 7, '2010-03-04', 'rabies shot');
INSERT IGNORE INTO visits VALUES (2, 8, '2011-03-04', 'rabies shot');
INSERT IGNORE INTO visits VALUES (3, 8, '2009-06-04', 'neutered');
INSERT IGNORE INTO visits VALUES (4, 7, '2008-09-04', 'spayed');

INSERT IGNORE INTO appointment_types VALUES (1, 'Checkup', 30, NULL, 'General wellness exam', 0);
INSERT IGNORE INTO appointment_types VALUES (2, 'Vaccination', 30, NULL, 'Standard vaccination visit', 0);
INSERT IGNORE INTO appointment_types VALUES (3, 'Surgery', 90, 2, 'Surgical procedure', 0);
INSERT IGNORE INTO appointment_types VALUES (4, 'Dental Cleaning', 60, 3, 'Professional dental cleaning', 0);
INSERT IGNORE INTO appointment_types VALUES (5, 'X-Ray', 30, 1, 'Diagnostic imaging', 0);
INSERT IGNORE INTO appointment_types VALUES (6, 'Emergency', 60, NULL, 'Emergency visit', 0);

INSERT IGNORE INTO clinic_schedule_config VALUES (1, 1, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT IGNORE INTO clinic_schedule_config VALUES (2, 2, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT IGNORE INTO clinic_schedule_config VALUES (3, 3, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT IGNORE INTO clinic_schedule_config VALUES (4, 4, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT IGNORE INTO clinic_schedule_config VALUES (5, 5, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT IGNORE INTO clinic_schedule_config VALUES (6, 6, '09:00:00', '13:00:00', 30, TRUE, 0);
INSERT IGNORE INTO clinic_schedule_config VALUES (7, 7, '09:00:00', '13:00:00', 30, FALSE, 0);

INSERT IGNORE INTO vet_schedules VALUES (1, 1, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (2, 1, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (3, 1, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (4, 1, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (5, 1, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (6, 1, 6, '09:00:00', '13:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (7, 2, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (8, 2, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (9, 2, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (10, 2, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (11, 2, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (12, 3, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (13, 3, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (14, 3, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (15, 3, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (16, 3, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (17, 3, 6, '09:00:00', '13:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (18, 4, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (19, 4, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (20, 4, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (21, 4, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (22, 4, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (23, 5, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (24, 5, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (25, 5, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (26, 5, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (27, 5, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (28, 6, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (29, 6, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (30, 6, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (31, 6, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (32, 6, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT IGNORE INTO vet_schedules VALUES (33, 6, 6, '09:00:00', '13:00:00', TRUE, 0);

INSERT IGNORE INTO vet_time_off VALUES (1, 2, '2026-04-06', 'Conference', 0);
INSERT IGNORE INTO vet_time_off VALUES (2, 4, '2026-04-07', 'Vacation day', 0);

INSERT IGNORE INTO appointments VALUES (1, 0, '2026-04-06', '09:00:00', '09:30:00', 'SCHEDULED', NULL, 1, 1, 1, NOW(), NULL);
INSERT IGNORE INTO appointments VALUES (2, 0, '2026-04-06', '10:00:00', '10:30:00', 'CONFIRMED', NULL, 7, 1, 2, NOW(), NULL);
INSERT IGNORE INTO appointments VALUES (3, 0, '2026-04-07', '09:00:00', '10:30:00', 'SCHEDULED', NULL, 3, 3, 3, NOW(), NULL);
INSERT IGNORE INTO appointments VALUES (4, 0, '2026-04-07', '14:00:00', '14:30:00', 'SCHEDULED', NULL, 5, 1, 1, NOW(), NULL);
