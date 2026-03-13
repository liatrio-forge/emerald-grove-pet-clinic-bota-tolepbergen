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

import java.time.LocalDate;
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
 * Integration tests for {@link VetTimeOffRepository} using H2 in-memory database with
 * seed data from SCH-02.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class VetTimeOffRepositoryTests {

	@Autowired
	private VetTimeOffRepository vetTimeOffRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void shouldFindByVetIdAndDateForHelenLearyConference() {
		Optional<VetTimeOff> result = vetTimeOffRepository.findByVetIdAndDate(2, LocalDate.of(2026, 4, 6));
		assertThat(result).isPresent();
		assertThat(result.get().getReason()).isEqualTo("Conference");
		// Access vet to verify JOIN FETCH worked
		assertThat(result.get().getVet().getFirstName()).isEqualTo("Helen");
	}

	@Test
	void shouldReturnTrueForExistingTimeOff() {
		assertThat(vetTimeOffRepository.existsByVetIdAndDate(2, LocalDate.of(2026, 4, 6))).isTrue();
	}

	@Test
	void shouldReturnFalseWhenNoTimeOffExists() {
		// James Carter (id=1) has no time off on 2026-04-06
		assertThat(vetTimeOffRepository.existsByVetIdAndDate(1, LocalDate.of(2026, 4, 6))).isFalse();
	}

	@Test
	void shouldFindTimeOffWithinDateRange() {
		List<VetTimeOff> result = vetTimeOffRepository.findByVetIdAndDateBetween(2, LocalDate.of(2026, 4, 1),
				LocalDate.of(2026, 4, 30));
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 4, 6));
	}

	@Test
	void shouldFindAllTimeOffByVetId() {
		List<VetTimeOff> result = vetTimeOffRepository.findByVetId(2);
		assertThat(result).hasSizeGreaterThanOrEqualTo(1);
	}

	@Test
	void shouldFindAllVetsOffOnSpecificDate() {
		List<VetTimeOff> result = vetTimeOffRepository.findByDate(LocalDate.of(2026, 4, 6));
		assertThat(result).hasSize(1);
		// Verify vet is accessible without LazyInitializationException
		assertThat(result.get(0).getVet().getFirstName()).isEqualTo("Helen");
	}

	@Test
	void shouldLoadVetNameWithoutLazyInitializationException() {
		Optional<VetTimeOff> result = vetTimeOffRepository.findByVetIdAndDate(2, LocalDate.of(2026, 4, 6));
		assertThat(result).isPresent();
		// Access vet - should not throw LazyInitializationException
		assertThat(result.get().getVet().getFirstName()).isEqualTo("Helen");
	}

	@Test
	@Transactional
	void shouldThrowDataIntegrityViolationExceptionForDuplicateVetDate() {
		// vet_id=2, date=2026-04-06 already exists (Helen Leary Conference)
		Vet vet = entityManager.find(Vet.class, 2);
		VetTimeOff duplicate = new VetTimeOff();
		duplicate.setVet(vet);
		duplicate.setDate(LocalDate.of(2026, 4, 6));
		duplicate.setReason("Another reason");
		assertThatThrownBy(() -> {
			vetTimeOffRepository.save(duplicate);
			vetTimeOffRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@Transactional
	void shouldSaveNewVetTimeOffWithVersionZero() {
		Vet vet = entityManager.find(Vet.class, 1);
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setVet(vet);
		timeOff.setDate(LocalDate.of(2026, 5, 1));
		timeOff.setReason("Sick day");

		VetTimeOff saved = vetTimeOffRepository.save(timeOff);
		vetTimeOffRepository.flush();

		assertThat(saved.getVersion()).isEqualTo(0);
		assertThat(saved.getId()).isNotNull();
	}

	@Test
	@Transactional
	void shouldFindTimeOffOrderedByDateAscending() {
		// Create additional time-off for Ortega (id=4) with an earlier date
		Vet ortega = entityManager.find(Vet.class, 4);
		VetTimeOff earlier = new VetTimeOff();
		earlier.setVet(ortega);
		earlier.setDate(LocalDate.of(2026, 4, 1));
		earlier.setReason("Personal day");
		vetTimeOffRepository.save(earlier);
		vetTimeOffRepository.flush();

		List<VetTimeOff> result = vetTimeOffRepository.findByVetId(4);
		assertThat(result).hasSizeGreaterThan(1);
		// Verify ascending order
		for (int i = 1; i < result.size(); i++) {
			assertThat(result.get(i).getDate()).isAfterOrEqualTo(result.get(i - 1).getDate());
		}
	}

}
