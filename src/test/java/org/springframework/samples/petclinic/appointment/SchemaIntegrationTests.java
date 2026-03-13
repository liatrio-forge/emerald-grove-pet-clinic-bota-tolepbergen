/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests verifying that the scheduling schema tables exist and have the
 * correct structure, constraints, and indexes in the H2 profile.
 *
 * <p>
 * RED phase: These tests will fail until the schema SQL files are updated with the 5 new
 * scheduling tables.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class SchemaIntegrationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// -------------------------------------------------------------------------
	// Table existence tests
	// -------------------------------------------------------------------------

	@Test
	void appointmentTypesTableExists() {
		// Act & Assert
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment_types", Long.class);
		assertThat(count).isNotNull().isGreaterThanOrEqualTo(0L);
	}

	@Test
	void appointmentsTableExists() {
		// Act & Assert
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments", Long.class);
		assertThat(count).isNotNull().isGreaterThanOrEqualTo(0L);
	}

	@Test
	void clinicScheduleConfigTableExists() {
		// Act & Assert
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clinic_schedule_config", Long.class);
		assertThat(count).isNotNull().isGreaterThanOrEqualTo(0L);
	}

	@Test
	void vetSchedulesTableExists() {
		// Act & Assert
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vet_schedules", Long.class);
		assertThat(count).isNotNull().isGreaterThanOrEqualTo(0L);
	}

	@Test
	void vetTimeOffTableExists() {
		// Act & Assert
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vet_time_off", Long.class);
		assertThat(count).isNotNull().isGreaterThanOrEqualTo(0L);
	}

	// -------------------------------------------------------------------------
	// Index existence tests (via H2 INFORMATION_SCHEMA)
	// -------------------------------------------------------------------------

	@Test
	void idxAppointmentVetDateExists() {
		// Arrange & Act
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES "
						+ "WHERE TABLE_NAME = 'APPOINTMENTS' AND INDEX_NAME = 'IDX_APPOINTMENT_VET_DATE'",
				Integer.class);
		// Assert
		assertThat(count).isNotNull().isGreaterThan(0);
	}

	@Test
	void idxAppointmentPetDateExists() {
		// Arrange & Act
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES "
						+ "WHERE TABLE_NAME = 'APPOINTMENTS' AND INDEX_NAME = 'IDX_APPOINTMENT_PET_DATE'",
				Integer.class);
		// Assert
		assertThat(count).isNotNull().isGreaterThan(0);
	}

	@Test
	void idxAppointmentStatusExists() {
		// Arrange & Act
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES "
				+ "WHERE TABLE_NAME = 'APPOINTMENTS' AND INDEX_NAME = 'IDX_APPOINTMENT_STATUS'", Integer.class);
		// Assert
		assertThat(count).isNotNull().isGreaterThan(0);
	}

	// -------------------------------------------------------------------------
	// Unique constraint tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void clinicScheduleConfigDayOfWeekUniqueConstraintEnforced() {
		// Arrange — seed data already has day_of_week = 1 (Monday)
		// Act & Assert — inserting a duplicate day_of_week = 1 should fail
		assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO clinic_schedule_config "
				+ "(day_of_week, open_time, close_time, slot_duration_minutes, is_open, version) "
				+ "VALUES (1, '08:00:00', '18:00:00', 30, TRUE, 0)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void vetSchedulesCompositeUniqueConstraintEnforced() {
		// Arrange — seed data already has vet_id=1, day_of_week=1
		// Act & Assert — inserting a duplicate (vet_id=1, day_of_week=1) should fail
		assertThatThrownBy(() -> jdbcTemplate
			.update("INSERT INTO vet_schedules " + "(vet_id, day_of_week, start_time, end_time, is_available, version) "
					+ "VALUES (1, 1, '10:00:00', '16:00:00', TRUE, 0)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void vetTimeOffCompositeUniqueConstraintEnforced() {
		// Arrange — insert first time-off row for vet 1, date 2026-04-06
		jdbcTemplate.update("INSERT INTO vet_time_off (vet_id, off_date, version) VALUES (1, '2026-04-06', 0)");

		// Act & Assert — second insert with same vet_id and off_date should fail
		assertThatThrownBy(() -> jdbcTemplate
			.update("INSERT INTO vet_time_off (vet_id, off_date, version) VALUES (1, '2026-04-06', 0)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	// -------------------------------------------------------------------------
	// Foreign key constraint tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void appointmentTypeNameUniqueConstraintEnforced() {
		// Arrange — seed data already has 'Checkup' in appointment_types
		// Act & Assert — inserting a duplicate name should fail
		assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO appointment_types "
				+ "(name, default_duration_minutes, version) VALUES ('Checkup', 45, 0)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void appointmentForeignKeyConstraintEnforcedForInvalidPetId() {
		// Arrange — use existing 'Checkup' appointment_type from seed data
		Integer typeId = jdbcTemplate.queryForObject("SELECT id FROM appointment_types WHERE name = 'Checkup'",
				Integer.class);

		// Act & Assert — attempt insert with non-existent pet_id 9999
		assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO appointments "
				+ "(appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, "
				+ "created_at, version) " + "VALUES ('2026-06-01', '10:00:00', '11:00:00', 'SCHEDULED', 9999, 1, "
				+ typeId + ", CURRENT_TIMESTAMP, 0)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	// -------------------------------------------------------------------------
	// CHECK constraint negative tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void appointmentTimeRangeCheckConstraintEnforced() {
		// Arrange — get existing appointment_type id
		Integer typeId = jdbcTemplate.queryForObject("SELECT id FROM appointment_types WHERE name = 'Checkup'",
				Integer.class);

		// Act & Assert — end_time <= start_time should violate chk_appointment_time_range
		assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO appointments "
				+ "(appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, "
				+ "created_at, version) " + "VALUES ('2026-06-01', '10:00:00', '09:00:00', 'SCHEDULED', 1, 1, " + typeId
				+ ", CURRENT_TIMESTAMP, 0)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void appointmentStatusCheckConstraintEnforced() {
		// Arrange — get existing appointment_type id
		Integer typeId = jdbcTemplate.queryForObject("SELECT id FROM appointment_types WHERE name = 'Checkup'",
				Integer.class);

		// Act & Assert — invalid status value should violate chk_appointment_status
		assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO appointments "
				+ "(appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, "
				+ "created_at, version) " + "VALUES ('2026-06-01', '09:00:00', '10:00:00', 'INVALID_STATUS', 1, 1, "
				+ typeId + ", CURRENT_TIMESTAMP, 0)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void appointmentVersionCheckConstraintEnforced() {
		// Arrange — get existing appointment_type id
		Integer typeId = jdbcTemplate.queryForObject("SELECT id FROM appointment_types WHERE name = 'Checkup'",
				Integer.class);

		// Act & Assert — negative version should violate chk_appointment_version
		assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO appointments "
				+ "(appointment_date, start_time, end_time, status, pet_id, vet_id, appointment_type_id, "
				+ "created_at, version) " + "VALUES ('2026-06-01', '09:00:00', '10:00:00', 'SCHEDULED', 1, 1, " + typeId
				+ ", CURRENT_TIMESTAMP, -1)"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void clinicScheduleDayOfWeekLowerBoundCheckConstraintEnforced() {
		// Act & Assert — day_of_week = 0 violates chk_clinic_schedule_day_range (>= 1)
		assertThatThrownBy(
				() -> jdbcTemplate.update("UPDATE clinic_schedule_config SET day_of_week = 0 WHERE day_of_week = 1"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void clinicScheduleDayOfWeekUpperBoundCheckConstraintEnforced() {
		// Act & Assert — day_of_week = 8 violates chk_clinic_schedule_day_range (<= 7)
		assertThatThrownBy(
				() -> jdbcTemplate.update("UPDATE clinic_schedule_config SET day_of_week = 8 WHERE day_of_week = 1"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void clinicScheduleSlotDurationCheckConstraintEnforced() {
		// Act & Assert — slot_duration_minutes = 0 violates
		// chk_clinic_slot_duration_bounds (>= 5)
		assertThatThrownBy(() -> jdbcTemplate
			.update("UPDATE clinic_schedule_config SET slot_duration_minutes = 0 WHERE day_of_week = 1"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void clinicScheduleOpenCloseTimeCheckConstraintEnforced() {
		// Act & Assert — open_time >= close_time on an open day violates
		// chk_clinic_open_close
		assertThatThrownBy(() -> jdbcTemplate.update(
				"UPDATE clinic_schedule_config SET open_time = '17:00:00', close_time = '09:00:00' WHERE day_of_week = 1"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void appointmentStatusDefaultsToScheduled() {
		// Arrange — use existing 'Checkup' appointment_type from seed data
		Integer typeId = jdbcTemplate.queryForObject("SELECT id FROM appointment_types WHERE name = 'Checkup'",
				Integer.class);

		// Act — insert appointment without explicit status (relies on DEFAULT)
		jdbcTemplate.update("INSERT INTO appointments "
				+ "(appointment_date, start_time, end_time, pet_id, vet_id, appointment_type_id, "
				+ "created_at, version) " + "VALUES ('2026-06-15', '09:00:00', '09:30:00', 1, 1, " + typeId
				+ ", CURRENT_TIMESTAMP, 0)");

		// Assert — status should default to SCHEDULED
		String status = jdbcTemplate.queryForObject(
				"SELECT status FROM appointments WHERE appointment_date = '2026-06-15' AND start_time = '09:00:00'"
						+ " AND pet_id = 1 AND vet_id = 1",
				String.class);
		assertThat(status).isEqualTo("SCHEDULED");
	}

}
