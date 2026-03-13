INSERT INTO vets VALUES (default, 'James', 'Carter');
INSERT INTO vets VALUES (default, 'Helen', 'Leary');
INSERT INTO vets VALUES (default, 'Linda', 'Douglas');
INSERT INTO vets VALUES (default, 'Rafael', 'Ortega');
INSERT INTO vets VALUES (default, 'Henry', 'Stevens');
INSERT INTO vets VALUES (default, 'Sharon', 'Jenkins');

INSERT INTO specialties VALUES (default, 'radiology');
INSERT INTO specialties VALUES (default, 'surgery');
INSERT INTO specialties VALUES (default, 'dentistry');

INSERT INTO vet_specialties VALUES (2, 1);
INSERT INTO vet_specialties VALUES (3, 2);
INSERT INTO vet_specialties VALUES (3, 3);
INSERT INTO vet_specialties VALUES (4, 2);
INSERT INTO vet_specialties VALUES (5, 1);

INSERT INTO types VALUES (default, 'cat');
INSERT INTO types VALUES (default, 'dog');
INSERT INTO types VALUES (default, 'lizard');
INSERT INTO types VALUES (default, 'snake');
INSERT INTO types VALUES (default, 'bird');
INSERT INTO types VALUES (default, 'hamster');

INSERT INTO owners VALUES (default, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT INTO owners VALUES (default, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT INTO owners VALUES (default, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT INTO owners VALUES (default, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198');
INSERT INTO owners VALUES (default, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765');
INSERT INTO owners VALUES (default, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT INTO owners VALUES (default, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387');
INSERT INTO owners VALUES (default, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683');
INSERT INTO owners VALUES (default, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435');
INSERT INTO owners VALUES (default, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

INSERT INTO pets VALUES (default, 'Leo', '2010-09-07', 1, 1);
INSERT INTO pets VALUES (default, 'Basil', '2012-08-06', 6, 2);
INSERT INTO pets VALUES (default, 'Rosy', '2011-04-17', 2, 3);
INSERT INTO pets VALUES (default, 'Jewel', '2010-03-07', 2, 3);
INSERT INTO pets VALUES (default, 'Iggy', '2010-11-30', 3, 4);
INSERT INTO pets VALUES (default, 'George', '2010-01-20', 4, 5);
INSERT INTO pets VALUES (default, 'Samantha', '2012-09-04', 1, 6);
INSERT INTO pets VALUES (default, 'Max', '2012-09-04', 1, 6);
INSERT INTO pets VALUES (default, 'Lucky', '2011-08-06', 5, 7);
INSERT INTO pets VALUES (default, 'Mulligan', '2007-02-24', 2, 8);
INSERT INTO pets VALUES (default, 'Freddy', '2010-03-09', 5, 9);
INSERT INTO pets VALUES (default, 'Lucky', '2010-06-24', 2, 10);
INSERT INTO pets VALUES (default, 'Sly', '2012-06-08', 1, 10);

INSERT INTO visits VALUES (default, 7, '2013-01-01', 'rabies shot');
INSERT INTO visits VALUES (default, 8, '2013-01-02', 'rabies shot');
INSERT INTO visits VALUES (default, 8, '2013-01-03', 'neutered');
INSERT INTO visits VALUES (default, 7, '2013-01-04', 'spayed');

INSERT INTO appointment_types VALUES (default, 'Checkup', 30, NULL, 'General wellness exam', 0);
INSERT INTO appointment_types VALUES (default, 'Vaccination', 30, NULL, 'Standard vaccination visit', 0);
INSERT INTO appointment_types VALUES (default, 'Surgery', 90, 2, 'Surgical procedure', 0);
INSERT INTO appointment_types VALUES (default, 'Dental Cleaning', 60, 3, 'Professional dental cleaning', 0);
INSERT INTO appointment_types VALUES (default, 'X-Ray', 30, 1, 'Diagnostic imaging', 0);
INSERT INTO appointment_types VALUES (default, 'Emergency', 60, NULL, 'Emergency visit', 0);

INSERT INTO clinic_schedule_config VALUES (default, 1, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT INTO clinic_schedule_config VALUES (default, 2, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT INTO clinic_schedule_config VALUES (default, 3, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT INTO clinic_schedule_config VALUES (default, 4, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT INTO clinic_schedule_config VALUES (default, 5, '09:00:00', '17:00:00', 30, TRUE, 0);
INSERT INTO clinic_schedule_config VALUES (default, 6, '09:00:00', '13:00:00', 30, TRUE, 0);
INSERT INTO clinic_schedule_config VALUES (default, 7, '09:00:00', '13:00:00', 30, FALSE, 0);

INSERT INTO vet_schedules VALUES (default, 1, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 1, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 1, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 1, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 1, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 1, 6, '09:00:00', '13:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 2, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 2, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 2, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 2, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 2, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 3, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 3, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 3, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 3, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 3, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 3, 6, '09:00:00', '13:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 4, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 4, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 4, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 4, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 4, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 5, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 5, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 5, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 5, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 5, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 6, 1, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 6, 2, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 6, 3, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 6, 4, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 6, 5, '09:00:00', '17:00:00', TRUE, 0);
INSERT INTO vet_schedules VALUES (default, 6, 6, '09:00:00', '13:00:00', TRUE, 0);

INSERT INTO vet_time_off VALUES (default, 2, '2026-04-06', 'Conference', 0);
INSERT INTO vet_time_off VALUES (default, 4, '2026-04-07', 'Vacation day', 0);

INSERT INTO appointments VALUES (default, 0, '2026-04-06', '09:00:00', '09:30:00', 'SCHEDULED', NULL, 1, 1, 1, CURRENT_TIMESTAMP, NULL);
INSERT INTO appointments VALUES (default, 0, '2026-04-06', '10:00:00', '10:30:00', 'CONFIRMED', NULL, 7, 1, 2, CURRENT_TIMESTAMP, NULL);
INSERT INTO appointments VALUES (default, 0, '2026-04-07', '09:00:00', '10:30:00', 'SCHEDULED', NULL, 3, 3, 3, CURRENT_TIMESTAMP, NULL);
INSERT INTO appointments VALUES (default, 0, '2026-04-07', '14:00:00', '14:30:00', 'SCHEDULED', NULL, 5, 1, 1, CURRENT_TIMESTAMP, NULL);
