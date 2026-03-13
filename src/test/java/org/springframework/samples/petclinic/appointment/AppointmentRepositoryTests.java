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
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link AppointmentRepository} using H2 in-memory database with
 * seed data from SCH-02.
 *
 * <p>
 * Seed data contains 4 appointments:
 * <ul>
 * <li>id=1: 2026-04-06 09:00-09:30 SCHEDULED pet=1(Leo) vet=1(Carter)
 * type=1(Checkup)</li>
 * <li>id=2: 2026-04-06 10:00-10:30 CONFIRMED pet=7 vet=1(Carter) type=2(Vaccination)</li>
 * <li>id=3: 2026-04-07 09:00-10:30 SCHEDULED pet=3 vet=3(Douglas) type=3(Surgery)</li>
 * <li>id=4: 2026-04-07 14:00-14:30 SCHEDULED pet=5 vet=1(Carter) type=1(Checkup)</li>
 * </ul>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class AppointmentRepositoryTests {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private AppointmentTypeRepository appointmentTypeRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	// -------------------------------------------------------------------------
	// Vet overlap query tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void shouldDetectOverlappingAppointmentForSameVet() {
		// seed: Carter(1) has 09:00-09:30 on 2026-04-06
		List<Appointment> result = appointmentRepository.findOverlappingByVet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 0), LocalTime.of(9, 30), null);
		assertThat(result).hasSize(1);
	}

	@Test
	@Transactional
	void shouldReturnEmptyForNonOverlappingVetTime() {
		// 11:00-11:30 does not overlap Carter's 09:00-09:30 or 10:00-10:30
		List<Appointment> result = appointmentRepository.findOverlappingByVet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(11, 0), LocalTime.of(11, 30), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldExcludeCancelledAppointmentsFromVetOverlap() {
		// Cancel seed appointment 1
		Appointment appt = appointmentRepository.findById(1).orElseThrow();
		appt.setStatus(AppointmentStatus.CANCELLED);
		appointmentRepository.save(appt);
		appointmentRepository.flush();

		List<Appointment> result = appointmentRepository.findOverlappingByVet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 0), LocalTime.of(9, 30), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldDetectPartialTimeOverlapForVet() {
		// 09:15-10:00 partially overlaps Carter's 09:00-09:30
		List<Appointment> result = appointmentRepository.findOverlappingByVet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 15), LocalTime.of(10, 0), null);
		assertThat(result).hasSize(1);
	}

	@Test
	@Transactional
	void shouldExcludeSelfOverlapWithExcludeId() {
		// Appointment 1 for Carter on 09:00-09:30; exclude id=1 (self)
		List<Appointment> result = appointmentRepository.findOverlappingByVet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 0), LocalTime.of(9, 30), 1);
		assertThat(result).isEmpty();
	}

	// -------------------------------------------------------------------------
	// Pet overlap query tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void shouldDetectOverlappingAppointmentForSamePet() {
		// seed: pet=1(Leo) has 09:00-09:30 on 2026-04-06
		List<Appointment> result = appointmentRepository.findOverlappingByPet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 0), LocalTime.of(9, 30), null);
		assertThat(result).hasSize(1);
	}

	@Test
	@Transactional
	void shouldReturnEmptyForDifferentPetOverlapCheck() {
		// pet=1's slot 09:00-09:30 doesn't affect pet=2
		List<Appointment> result = appointmentRepository.findOverlappingByPet(2, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 0), LocalTime.of(9, 30), null);
		assertThat(result).isEmpty();
	}

	// -------------------------------------------------------------------------
	// Owner overlap query tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void shouldDetectOverlappingAppointmentForSameOwner() {
		// seed: pet=1(Leo) owner=1(George Franklin) has appointment on 2026-04-06
		// 09:00-09:30
		List<Appointment> result = appointmentRepository.findOverlappingByOwner(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 0), LocalTime.of(9, 30), null);
		assertThat(result).hasSize(1);
	}

	@Test
	@Transactional
	void shouldReturnEmptyForDifferentOwnerOverlapCheck() {
		// George Franklin (id=1) has pet with appointment on 2026-04-06 09:00-09:30
		// Different owner (e.g. id=2 Betty Davis) should have no overlap
		List<Appointment> result = appointmentRepository.findOverlappingByOwner(2, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 0), LocalTime.of(9, 30), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldNotDetectBoundaryOverlapForVetWhenNewStartsAtExistingEnd() {
		// 09:30-10:00 merely touches Carter's 09:00-09:30 at the boundary
		List<Appointment> result = appointmentRepository.findOverlappingByVet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 30), LocalTime.of(10, 0), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldNotDetectBoundaryOverlapForVetWhenNewEndsAtExistingStart() {
		// 08:30-09:00 merely touches Carter's 09:00-09:30 at the boundary
		List<Appointment> result = appointmentRepository.findOverlappingByVet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(8, 30), LocalTime.of(9, 0), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldNotDetectBoundaryOverlapForPetWhenNewStartsAtExistingEnd() {
		// pet=1(Leo) has 09:00-09:30; 09:30-10:00 merely touches
		List<Appointment> result = appointmentRepository.findOverlappingByPet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 30), LocalTime.of(10, 0), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldNotDetectBoundaryOverlapForOwnerWhenNewStartsAtExistingEnd() {
		// owner=1(George) has pet appointment 09:00-09:30; 09:30-10:00 merely touches
		List<Appointment> result = appointmentRepository.findOverlappingByOwner(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(9, 30), LocalTime.of(10, 0), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldNotDetectBoundaryOverlapForPetWhenNewEndsAtExistingStart() {
		// pet=1(Leo) has 09:00-09:30; 08:30-09:00 merely touches at the boundary
		List<Appointment> result = appointmentRepository.findOverlappingByPet(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(8, 30), LocalTime.of(9, 0), null);
		assertThat(result).isEmpty();
	}

	@Test
	@Transactional
	void shouldNotDetectBoundaryOverlapForOwnerWhenNewEndsAtExistingStart() {
		// owner=1(George) has pet appointment 09:00-09:30; 08:30-09:00 merely touches at
		// the boundary
		List<Appointment> result = appointmentRepository.findOverlappingByOwner(1, LocalDate.of(2026, 4, 6),
				LocalTime.of(8, 30), LocalTime.of(9, 0), null);
		assertThat(result).isEmpty();
	}

	// -------------------------------------------------------------------------
	// Calendar query tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void shouldFindAppointmentsByVetAndDateSortedByStartTime() {
		// Carter(1) has 1 appointment on 2026-04-07: 14:00
		List<Appointment> result = appointmentRepository.findByVetIdAndDate(1, LocalDate.of(2026, 4, 7));
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(14, 0));
	}

	@Test
	@Transactional
	void shouldFindAppointmentsByVetAndDateRange() {
		// Carter(1) has appointments on 2026-04-06 (2) and 2026-04-07 (1) = 3 total
		List<Appointment> result = appointmentRepository.findByVetIdAndDateBetween(1, LocalDate.of(2026, 4, 6),
				LocalDate.of(2026, 4, 7));
		assertThat(result).hasSize(3); // 2 on Apr 6, 1 on Apr 7
	}

	@Test
	@Transactional
	void shouldFindAllAppointmentsForADate() {
		// 2026-04-06: 2 appointments both for Carter(1)
		List<Appointment> result = appointmentRepository.findByDate(LocalDate.of(2026, 4, 6));
		assertThat(result).hasSize(2);
		// Both appointments are for the same vet (Carter)
		assertThat(result.get(0).getVet().getId()).isEqualTo(1);
		assertThat(result.get(1).getVet().getId()).isEqualTo(1);
	}

	@Test
	@Transactional
	void shouldFindByIdWithDetailsLoadingAllAssociations() {
		Optional<Appointment> result = appointmentRepository.findByIdWithDetails(1);
		assertThat(result).isPresent();
		PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();
		assertThat(persistenceUnitUtil.isLoaded(result.get(), "pet")).isTrue();
		assertThat(persistenceUnitUtil.isLoaded(result.get(), "vet")).isTrue();
		assertThat(persistenceUnitUtil.isLoaded(result.get(), "appointmentType")).isTrue();
	}

	// -------------------------------------------------------------------------
	// Persistence tests
	// -------------------------------------------------------------------------

	@Test
	@Transactional
	void shouldSaveNewAppointmentWithGeneratedIdAndVersionZero() {
		Appointment appointment = buildNewAppointment();
		Appointment saved = appointmentRepository.save(appointment);
		appointmentRepository.flush();

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getVersion()).isEqualTo(0);
	}

	@Test
	@Transactional
	void shouldSetCreatedAtAutomaticallyOnPrePersist() {
		Appointment appointment = buildNewAppointment();
		// createdAt not set - should be set by @PrePersist
		appointment.setCreatedAt(null);
		Appointment saved = appointmentRepository.save(appointment);
		appointmentRepository.flush();

		assertThat(saved.getCreatedAt()).isNotNull();
	}

	private Appointment buildNewAppointment() {
		Pet pet = entityManager.find(Pet.class, 1);
		Vet vet = entityManager.find(Vet.class, 1);
		AppointmentType type = appointmentTypeRepository.findByName("Checkup").orElseThrow();

		Appointment appointment = new Appointment();
		appointment.setAppointmentDate(LocalDate.of(2026, 6, 1));
		appointment.setStartTime(LocalTime.of(11, 0));
		appointment.setEndTime(LocalTime.of(11, 30));
		appointment.setPet(pet);
		appointment.setVet(vet);
		appointment.setAppointmentType(type);
		return appointment;
	}

}
