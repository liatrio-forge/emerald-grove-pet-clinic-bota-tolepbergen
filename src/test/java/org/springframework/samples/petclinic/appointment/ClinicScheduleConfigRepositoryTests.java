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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link ClinicScheduleConfigRepository} using H2 in-memory
 * database with seed data from SCH-02.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ClinicScheduleConfigRepositoryTests {

	@Autowired
	private ClinicScheduleConfigRepository clinicScheduleConfigRepository;

	@Test
	void shouldFindMondayConfigFromSeedData() {
		Optional<ClinicScheduleConfig> result = clinicScheduleConfigRepository.findByDayOfWeek(1);
		assertThat(result).isPresent();
		assertThat(result.get().getOpenTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(result.get().getCloseTime()).isEqualTo(LocalTime.of(17, 0));
		assertThat(result.get().getSlotDurationMinutes()).isEqualTo(30);
		assertThat(result.get().getIsOpen()).isTrue();
	}

	@Test
	void shouldFindSundayConfigWithIsOpenFalse() {
		Optional<ClinicScheduleConfig> result = clinicScheduleConfigRepository.findByDayOfWeek(7);
		assertThat(result).isPresent();
		assertThat(result.get().getIsOpen()).isFalse();
	}

	@Test
	void shouldReturnEmptyForInvalidDayOfWeek() {
		Optional<ClinicScheduleConfig> result = clinicScheduleConfigRepository.findByDayOfWeek(0);
		assertThat(result).isEmpty();
	}

	@Test
	void shouldReturnEmptyForInvalidUpperDayOfWeek() {
		Optional<ClinicScheduleConfig> result = clinicScheduleConfigRepository.findByDayOfWeek(8);
		assertThat(result).isEmpty();
	}

	@Test
	void shouldFindByIsOpenTrueReturns6Rows() {
		List<ClinicScheduleConfig> openDays = clinicScheduleConfigRepository.findByIsOpenTrue();
		assertThat(openDays).hasSize(6);
	}

	@Test
	void shouldFindAllOrderedByDayOfWeekReturns7Rows() {
		List<ClinicScheduleConfig> allConfigs = clinicScheduleConfigRepository.findAllByOrderByDayOfWeekAsc();
		assertThat(allConfigs).hasSize(7);
		assertThat(allConfigs.get(0).getDayOfWeek()).isEqualTo(1);
		assertThat(allConfigs.get(6).getDayOfWeek()).isEqualTo(7);
	}

	@Test
	@Transactional
	void shouldIncrementVersionOnUpdate() {
		Optional<ClinicScheduleConfig> monday = clinicScheduleConfigRepository.findByDayOfWeek(1);
		assertThat(monday).isPresent();
		ClinicScheduleConfig config = monday.get();
		Integer originalVersion = config.getVersion();

		config.setSlotDurationMinutes(15);
		clinicScheduleConfigRepository.save(config);
		clinicScheduleConfigRepository.flush();

		Optional<ClinicScheduleConfig> updated = clinicScheduleConfigRepository.findByDayOfWeek(1);
		assertThat(updated).isPresent();
		assertThat(updated.get().getVersion()).isEqualTo(originalVersion + 1);
	}

}
