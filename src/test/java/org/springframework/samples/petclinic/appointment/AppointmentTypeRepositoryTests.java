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

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link AppointmentTypeRepository} using H2 in-memory database
 * with seed data from SCH-02.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class AppointmentTypeRepositoryTests {

	@Autowired
	private AppointmentTypeRepository appointmentTypeRepository;

	@Test
	void shouldFindByNameCheckup() {
		Optional<AppointmentType> result = appointmentTypeRepository.findByName("Checkup");
		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Checkup");
		assertThat(result.get().getDefaultDurationMinutes()).isEqualTo(30);
	}

	@Test
	void shouldReturnEmptyWhenNameNotFound() {
		Optional<AppointmentType> result = appointmentTypeRepository.findByName("Nonexistent");
		assertThat(result).isNotPresent();
	}

	@Test
	void shouldFindAllWithSpecialtyReturns6Types() {
		List<AppointmentType> types = appointmentTypeRepository.findAllWithSpecialty();
		assertThat(types).hasSize(6);
	}

	@Test
	void shouldFindAllWithSpecialtyLoadsSpecialtyForSurgery() {
		List<AppointmentType> types = appointmentTypeRepository.findAllWithSpecialty();
		AppointmentType surgery = types.stream()
			.filter(t -> "Surgery".equals(t.getName()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Surgery not found"));
		// Surgery has required specialty (surgery) from seed data
		assertThat(surgery.getRequiredSpecialty()).isNotNull();
	}

	@Test
	void shouldFindAllWithSpecialtyLoadsSpecialtyForDentalCleaning() {
		List<AppointmentType> types = appointmentTypeRepository.findAllWithSpecialty();
		AppointmentType dental = types.stream()
			.filter(t -> "Dental Cleaning".equals(t.getName()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Dental Cleaning not found"));
		assertThat(dental.getRequiredSpecialty()).isNotNull();
	}

	@Test
	void shouldFindAllWithSpecialtyHasNullSpecialtyForCheckup() {
		List<AppointmentType> types = appointmentTypeRepository.findAllWithSpecialty();
		AppointmentType checkup = types.stream()
			.filter(t -> "Checkup".equals(t.getName()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Checkup not found"));
		assertThat(checkup.getRequiredSpecialty()).isNull();
	}

	@Test
	void shouldFindByIdWithSpecialtyForSurgery() {
		// Surgery is inserted 3rd in seed data, but auto-assigned ID may vary
		// Find Surgery by name first, then test findByIdWithSpecialty
		Optional<AppointmentType> surgeryByName = appointmentTypeRepository.findByName("Surgery");
		assertThat(surgeryByName).isPresent();
		Integer surgeryId = surgeryByName.get().getId();

		Optional<AppointmentType> result = appointmentTypeRepository.findByIdWithSpecialty(surgeryId);
		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Surgery");
		assertThat(result.get().getRequiredSpecialty()).isNotNull();
	}

	@Test
	@Transactional
	void shouldSaveAndRetrieveNewAppointmentType() {
		AppointmentType grooming = new AppointmentType();
		grooming.setName("Grooming");
		grooming.setDefaultDurationMinutes(45);

		AppointmentType saved = appointmentTypeRepository.save(grooming);

		assertThat(saved.getId()).isNotNull();
		Optional<AppointmentType> found = appointmentTypeRepository.findByName("Grooming");
		assertThat(found).isPresent();
		assertThat(found.get().getDefaultDurationMinutes()).isEqualTo(45);
	}

}
