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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Integration tests verifying that the scheduling seed data is correctly loaded for all
 * scheduling tables in the H2 default profile.
 *
 * <p>
 * Tests follow the Arrange-Act-Assert pattern and verify counts, values, and FK
 * references for all seed data rows introduced in SCH-02.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class SeedDataIntegrationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// -------------------------------------------------------------------------
	// appointment_types
	// -------------------------------------------------------------------------

	@Test
	void appointmentTypesShouldHaveSixRows() {
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment_types", Integer.class);
		assertThat(count).isEqualTo(6);
	}

	@Test
	void appointmentTypesShouldContainCheckup() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointment_types WHERE name = 'Checkup' AND default_duration_minutes = 30 AND specialty_id IS NULL",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentTypesShouldContainVaccination() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointment_types WHERE name = 'Vaccination' AND default_duration_minutes = 30 AND specialty_id IS NULL",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentTypesShouldContainSurgeryWithSurgerySpecialty() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointment_types WHERE name = 'Surgery' AND default_duration_minutes = 90 AND specialty_id = 2",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentTypesShouldContainDentalCleaningWithDentistrySpecialty() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointment_types WHERE name = 'Dental Cleaning' AND default_duration_minutes = 60 AND specialty_id = 3",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentTypesShouldContainXRayWithRadiologySpecialty() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointment_types WHERE name = 'X-Ray' AND default_duration_minutes = 30 AND specialty_id = 1",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentTypesShouldContainEmergency() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointment_types WHERE name = 'Emergency' AND default_duration_minutes = 60 AND specialty_id IS NULL",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentTypesSpecialtyFkShouldReferenceCorrectSpecialtyNames() {
		// Surgery type references specialty named 'surgery'
		String surgeryName = jdbcTemplate.queryForObject(
				"SELECT s.name FROM appointment_types at JOIN specialties s ON at.specialty_id = s.id WHERE at.name = 'Surgery'",
				String.class);
		assertThat(surgeryName).isEqualTo("surgery");

		// Dental Cleaning references 'dentistry'
		String dentistryName = jdbcTemplate.queryForObject(
				"SELECT s.name FROM appointment_types at JOIN specialties s ON at.specialty_id = s.id WHERE at.name = 'Dental Cleaning'",
				String.class);
		assertThat(dentistryName).isEqualTo("dentistry");

		// X-Ray references 'radiology'
		String radiologyName = jdbcTemplate.queryForObject(
				"SELECT s.name FROM appointment_types at JOIN specialties s ON at.specialty_id = s.id WHERE at.name = 'X-Ray'",
				String.class);
		assertThat(radiologyName).isEqualTo("radiology");
	}

	// -------------------------------------------------------------------------
	// clinic_schedule_config
	// -------------------------------------------------------------------------

	@Test
	void clinicScheduleConfigShouldHaveSevenRows() {
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clinic_schedule_config", Integer.class);
		assertThat(count).isEqualTo(7);
	}

	@Test
	void clinicScheduleConfigWeekdaysShouldBeOpen0900To1700() {
		// Monday=1 through Friday=5
		for (int day = 1; day <= 5; day++) {
			final int dayOfWeek = day;
			Integer count = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM clinic_schedule_config WHERE day_of_week = ? AND open_time = '09:00:00' AND close_time = '17:00:00' AND is_open = TRUE AND slot_duration_minutes = 30",
					Integer.class, dayOfWeek);
			assertThat(count).as("Expected weekday %d to have open hours 09:00-17:00", dayOfWeek).isEqualTo(1);
		}
	}

	@Test
	void clinicScheduleConfigSaturdayShouldBeOpen0900To1300() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM clinic_schedule_config WHERE day_of_week = 6 AND open_time = '09:00:00' AND close_time = '13:00:00' AND is_open = TRUE AND slot_duration_minutes = 30",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void clinicScheduleConfigSundayShouldBeClosed() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM clinic_schedule_config WHERE day_of_week = 7 AND is_open = FALSE", Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void clinicScheduleConfigAllDaysShouldHaveSlotDuration30() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM clinic_schedule_config WHERE slot_duration_minutes = 30", Integer.class);
		assertThat(count).isEqualTo(7);
	}

	// -------------------------------------------------------------------------
	// vet_schedules
	// -------------------------------------------------------------------------

	@Test
	void vetSchedulesShouldHaveThirtyThreeRows() {
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vet_schedules", Integer.class);
		assertThat(count).isEqualTo(33);
	}

	@Test
	void vetSchedulesAllSixVetsShouldHaveWeekdayEntries() {
		// Each vet (id 1-6) should have exactly 5 weekday rows
		for (int vetId = 1; vetId <= 6; vetId++) {
			final int id = vetId;
			Integer count = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM vet_schedules WHERE vet_id = ? AND day_of_week BETWEEN 1 AND 5",
					Integer.class, id);
			assertThat(count).as("Expected vet %d to have 5 weekday schedule rows", id).isEqualTo(5);
		}
	}

	@Test
	void vetSchedulesWeekdayEntriesShouldBe0900To1700AndAvailable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM vet_schedules WHERE day_of_week BETWEEN 1 AND 5 AND start_time = '09:00:00' AND end_time = '17:00:00' AND is_available = TRUE",
				Integer.class);
		// 6 vets x 5 weekdays = 30 rows
		assertThat(count).isEqualTo(30);
	}

	@Test
	void vetSchedulesSaturdayVetsShouldBeCarterDouglasJenkins() {
		// Only vets 1 (Carter), 3 (Douglas), 6 (Jenkins) work Saturdays
		List<Map<String, Object>> saturdayRows = jdbcTemplate.queryForList(
				"SELECT vet_id, start_time, end_time FROM vet_schedules WHERE day_of_week = 6 ORDER BY vet_id");
		assertThat(saturdayRows).hasSize(3);

		List<Object> satVetIds = saturdayRows.stream().map(row -> row.get("VET_ID")).toList();
		assertThat(satVetIds).containsExactly(1, 3, 6);
	}

	@Test
	void vetSchedulesSaturdayEntriesShouldBe0900To1300() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM vet_schedules WHERE day_of_week = 6 AND start_time = '09:00:00' AND end_time = '13:00:00' AND is_available = TRUE",
				Integer.class);
		assertThat(count).isEqualTo(3);
	}

	@Test
	void vetSchedulesVetsWithoutSaturdayShouldNotHaveSaturdayEntry() {
		// Vets 2, 4, 5 should NOT have a Saturday row
		for (int vetId : new int[] { 2, 4, 5 }) {
			Integer count = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM vet_schedules WHERE vet_id = ? AND day_of_week = 6", Integer.class, vetId);
			assertThat(count).as("Expected vet %d to have no Saturday schedule entry", vetId).isEqualTo(0);
		}
	}

	// -------------------------------------------------------------------------
	// vet_time_off
	// -------------------------------------------------------------------------

	@Test
	void vetTimeOffShouldHaveAtLeastTwoRows() {
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vet_time_off", Integer.class);
		assertThat(count).isGreaterThanOrEqualTo(2);
	}

	@Test
	void vetTimeOffHelenLearyOnApril6ShouldHaveReasonConference() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM vet_time_off WHERE vet_id = 2 AND off_date = '2026-04-06' AND reason = 'Conference'",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void vetTimeOffRafaelOrtegaOnApril7ShouldHaveReasonVacationDay() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM vet_time_off WHERE vet_id = 4 AND off_date = '2026-04-07' AND reason = 'Vacation day'",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	// -------------------------------------------------------------------------
	// appointments
	// -------------------------------------------------------------------------

	@Test
	void appointmentsShouldHaveAtLeastFourRows() {
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments", Integer.class);
		assertThat(count).isGreaterThanOrEqualTo(4);
	}

	@Test
	void appointmentLeoWithCarterOnApril6ShouldBeScheduled() {
		// Pet 1 (Leo), Vet 1 (Carter), Type 1 (Checkup), Date 2026-04-06, Start 09:00
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointments WHERE pet_id = 1 AND vet_id = 1 AND appointment_type_id = 1 AND appointment_date = '2026-04-06' AND start_time = '09:00:00' AND end_time = '09:30:00' AND status = 'SCHEDULED'",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentSamanthaWithCarterOnApril6ShouldBeConfirmed() {
		// Pet 7 (Samantha), Vet 1 (Carter), Type 2 (Vaccination), Date 2026-04-06, Start
		// 10:00
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointments WHERE pet_id = 7 AND vet_id = 1 AND appointment_type_id = 2 AND appointment_date = '2026-04-06' AND start_time = '10:00:00' AND end_time = '10:30:00' AND status = 'CONFIRMED'",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentRosyWithDouglasOnApril7ShouldBeScheduled() {
		// Pet 3 (Rosy), Vet 3 (Douglas), Type 3 (Surgery), Date 2026-04-07, Start 09:00,
		// End 10:30
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointments WHERE pet_id = 3 AND vet_id = 3 AND appointment_type_id = 3 AND appointment_date = '2026-04-07' AND start_time = '09:00:00' AND end_time = '10:30:00' AND status = 'SCHEDULED'",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentIggyWithCarterOnApril7ShouldBeScheduled() {
		// Pet 5 (Iggy), Vet 1 (Carter), Type 1 (Checkup), Date 2026-04-07, Start 14:00,
		// End 14:30
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM appointments WHERE pet_id = 5 AND vet_id = 1 AND appointment_type_id = 1 AND appointment_date = '2026-04-07' AND start_time = '14:00:00' AND end_time = '14:30:00' AND status = 'SCHEDULED'",
				Integer.class);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void appointmentEndTimesShouldMatchTypeDurations() {
		// Leo's Checkup (30 min): 09:00 -> 09:30
		Map<String, Object> leoAppt = jdbcTemplate.queryForMap(
				"SELECT start_time, end_time FROM appointments WHERE pet_id = 1 AND appointment_date = '2026-04-06' AND start_time = '09:00:00'");
		assertThat(LocalTime.parse(leoAppt.get("START_TIME").toString())).isEqualTo(LocalTime.of(9, 0));
		assertThat(LocalTime.parse(leoAppt.get("END_TIME").toString())).isEqualTo(LocalTime.of(9, 30));

		// Rosy's Surgery (90 min): 09:00 -> 10:30
		Map<String, Object> rosyAppt = jdbcTemplate.queryForMap(
				"SELECT start_time, end_time FROM appointments WHERE pet_id = 3 AND appointment_date = '2026-04-07' AND start_time = '09:00:00'");
		assertThat(LocalTime.parse(rosyAppt.get("START_TIME").toString())).isEqualTo(LocalTime.of(9, 0));
		assertThat(LocalTime.parse(rosyAppt.get("END_TIME").toString())).isEqualTo(LocalTime.of(10, 30));
	}

}
