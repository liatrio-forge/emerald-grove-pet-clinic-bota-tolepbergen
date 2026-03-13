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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link VetScheduleRepository} using H2 in-memory database with
 * seed data from SCH-02.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class VetScheduleRepositoryTests {

	@Autowired
	private VetScheduleRepository vetScheduleRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void shouldFindSchedulesForJamesCarter() {
		List<VetSchedule> schedules = vetScheduleRepository.findByVetId(1);
		// James Carter works Mon-Sat (6 days)
		assertThat(schedules).hasSize(6);
	}

	@Test
	void shouldFindMondayScheduleForCarter() {
		Optional<VetSchedule> result = vetScheduleRepository.findByVetIdAndDayOfWeek(1, 1);
		assertThat(result).isPresent();
		assertThat(result.get().getStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(result.get().getEndTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	void shouldReturnEmptyForHelenLearySaturday() {
		// Helen Leary (id=2) doesn't work Saturdays per seed data
		Optional<VetSchedule> result = vetScheduleRepository.findByVetIdAndDayOfWeek(2, 6);
		assertThat(result).isEmpty();
	}

	@Test
	void shouldFindAvailableVetsOnSaturday() {
		// Saturday (day=6): Carter(1), Douglas(3), Jenkins(6)
		List<VetSchedule> saturdayVets = vetScheduleRepository.findAvailableByDayOfWeek(6);
		assertThat(saturdayVets).hasSize(3);
		List<Integer> vetIds = saturdayVets.stream().map(vs -> vs.getVet().getId()).toList();
		assertThat(vetIds).containsExactlyInAnyOrder(1, 3, 6);
	}

	@Test
	void shouldFindAvailableDaysForCarter() {
		List<VetSchedule> carterDays = vetScheduleRepository.findAvailableByVetId(1);
		assertThat(carterDays).hasSize(6); // Mon-Sat
		// Should be ordered by dayOfWeek
		for (int i = 1; i < carterDays.size(); i++) {
			assertThat(carterDays.get(i).getDayOfWeek()).isGreaterThan(carterDays.get(i - 1).getDayOfWeek());
		}
	}

	@Test
	void shouldLoadVetNameWithoutLazyInitializationException() {
		List<VetSchedule> schedules = vetScheduleRepository.findByVetId(1);
		assertThat(schedules).isNotEmpty();
		// Access vet name - should not throw LazyInitializationException
		String firstName = schedules.get(0).getVet().getFirstName();
		assertThat(firstName).isEqualTo("James");
	}

	@Test
	@Transactional
	void shouldThrowDataIntegrityViolationExceptionForDuplicateVetDay() {
		// vet_id=1, day_of_week=1 already exists in seed data
		Vet vet = entityManager.find(Vet.class, 1);
		VetSchedule duplicate = new VetSchedule();
		duplicate.setVet(vet);
		duplicate.setDayOfWeek(1); // Monday - already exists for Carter
		duplicate.setStartTime(LocalTime.of(10, 0));
		duplicate.setEndTime(LocalTime.of(16, 0));
		duplicate.setIsAvailable(true);
		assertThatThrownBy(() -> {
			vetScheduleRepository.save(duplicate);
			vetScheduleRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void shouldSaveNewVetScheduleWithVersionZero() {
		// Sunday (day=7) for Carter - doesn't exist in seed data
		Vet vet = entityManager.find(Vet.class, 1);
		VetSchedule sunday = new VetSchedule();
		sunday.setVet(vet);
		sunday.setDayOfWeek(7);
		sunday.setStartTime(LocalTime.of(10, 0));
		sunday.setEndTime(LocalTime.of(14, 0));
		sunday.setIsAvailable(false);

		VetSchedule saved = vetScheduleRepository.save(sunday);
		vetScheduleRepository.flush();

		assertThat(saved.getVersion()).isEqualTo(0);
		assertThat(saved.getId()).isNotNull();
	}

}
