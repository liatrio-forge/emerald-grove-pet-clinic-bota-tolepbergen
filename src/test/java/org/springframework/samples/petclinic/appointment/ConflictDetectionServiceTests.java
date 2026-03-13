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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;

/**
 * Unit tests for {@link ConflictDetectionService}. Mockito-based, 100% branch coverage
 * required.
 */
@ExtendWith(MockitoExtension.class)
class ConflictDetectionServiceTests {

	@Mock
	private AppointmentRepository appointmentRepo;

	@Mock
	private ClinicScheduleConfigRepository clinicConfigRepo;

	@Mock
	private VetScheduleRepository vetScheduleRepo;

	@Mock
	private VetTimeOffRepository vetTimeOffRepo;

	@InjectMocks
	private ConflictDetectionService service;

	private static final LocalDate MONDAY = LocalDate.of(2026, 4, 6);

	private static final LocalDate SUNDAY = LocalDate.of(2026, 4, 5);

	private static final Integer VET_ID = 1;

	private static final Integer PET_ID = 1;

	private static final Integer OWNER_ID = 1;

	private static final LocalTime START = LocalTime.of(9, 0);

	private static final LocalTime END = LocalTime.of(9, 30);

	private ClinicScheduleConfig openConfig;

	private VetSchedule availableSchedule;

	private Vet vet;

	private AppointmentType checkupType;

	@BeforeEach
	void setUp() {
		openConfig = new ClinicScheduleConfig();
		openConfig.setDayOfWeek(1);
		openConfig.setIsOpen(true);
		openConfig.setOpenTime(LocalTime.of(9, 0));
		openConfig.setCloseTime(LocalTime.of(17, 0));
		openConfig.setSlotDurationMinutes(30);

		availableSchedule = new VetSchedule();
		availableSchedule.setDayOfWeek(1);
		availableSchedule.setIsAvailable(true);
		availableSchedule.setStartTime(LocalTime.of(9, 0));
		availableSchedule.setEndTime(LocalTime.of(17, 0));

		vet = new Vet();
		vet.setId(VET_ID);
		vet.setFirstName("James");
		vet.setLastName("Carter");

		checkupType = new AppointmentType();
		checkupType.setName("Checkup");
		checkupType.setDefaultDurationMinutes(30);
		checkupType.setRequiredSpecialty(null);
	}

	// Convenience method to stub "all clear" for standard checks
	private void stubAllClear() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());
	}

	@Test
	void noConflictsForValidAppointment() {
		stubAllClear();

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.hasConflicts()).isFalse();
		assertThat(result.conflicts()).isEmpty();
	}

	@Test
	void clinicClosedReturnsEarly() {
		ClinicScheduleConfig closedConfig = new ClinicScheduleConfig();
		closedConfig.setDayOfWeek(7);
		closedConfig.setIsOpen(false);
		closedConfig.setOpenTime(LocalTime.of(9, 0));
		closedConfig.setCloseTime(LocalTime.of(17, 0));
		closedConfig.setSlotDurationMinutes(30);
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(closedConfig));

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, SUNDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.CLINIC_CLOSED)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.CLINIC_CLOSED).get(0).message())
			.contains("Clinic is closed on Sunday");
		// Only 1 conflict because method returns early
		assertThat(result.conflicts()).hasSize(1);
		// Downstream repos must not be queried when clinic is closed
		verifyNoInteractions(appointmentRepo, vetScheduleRepo, vetTimeOffRepo);
	}

	@Test
	void clinicConfigMissingReturnsClinicClosedConflict() {
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.empty());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, SUNDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.CLINIC_CLOSED)).hasSize(1);
	}

	@Test
	void outsideClinicHoursStartBeforeOpen() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		LocalTime earlyStart = LocalTime.of(8, 0);
		LocalTime earlyEnd = LocalTime.of(8, 30);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, earlyStart, earlyEnd, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, earlyStart, earlyEnd, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, earlyStart, earlyEnd, null))
			.willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, earlyStart, earlyEnd,
				checkupType, vet, null);

		assertThat(result.getConflictsByType(ConflictType.OUTSIDE_CLINIC_HOURS)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.OUTSIDE_CLINIC_HOURS).get(0).message())
			.contains("outside clinic hours");
	}

	@Test
	void outsideClinicHoursEndAfterClose() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		LocalTime lateStart = LocalTime.of(16, 30);
		LocalTime lateEnd = LocalTime.of(17, 30);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, lateStart, lateEnd, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, lateStart, lateEnd, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, lateStart, lateEnd, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, lateStart, lateEnd,
				checkupType, vet, null);

		assertThat(result.getConflictsByType(ConflictType.OUTSIDE_CLINIC_HOURS)).hasSize(1);
	}

	@Test
	void endsExactlyAtClinicCloseIsAllowed() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		LocalTime boundaryStart = LocalTime.of(16, 30);
		LocalTime boundaryEnd = LocalTime.of(17, 0); // exactly at close
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, boundaryStart, boundaryEnd, null))
			.willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, boundaryStart, boundaryEnd, null))
			.willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, boundaryStart, boundaryEnd, null))
			.willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, boundaryStart, boundaryEnd,
				checkupType, vet, null);

		assertThat(result.getConflictsByType(ConflictType.OUTSIDE_CLINIC_HOURS)).isEmpty();
	}

	@Test
	void vetNotScheduledReturnsVetNotAvailable() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.empty());
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.VET_NOT_AVAILABLE)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.VET_NOT_AVAILABLE).get(0).message())
			.contains("does not work on Monday");
	}

	@Test
	void vetScheduleExistsButNotAvailable() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		VetSchedule unavailableSchedule = new VetSchedule();
		unavailableSchedule.setDayOfWeek(1);
		unavailableSchedule.setIsAvailable(false);
		unavailableSchedule.setStartTime(LocalTime.of(9, 0));
		unavailableSchedule.setEndTime(LocalTime.of(17, 0));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(unavailableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.VET_NOT_AVAILABLE)).hasSize(1);
	}

	@Test
	void outsideVetHours() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		VetSchedule narrowSchedule = new VetSchedule();
		narrowSchedule.setDayOfWeek(1);
		narrowSchedule.setIsAvailable(true);
		narrowSchedule.setStartTime(LocalTime.of(10, 0));
		narrowSchedule.setEndTime(LocalTime.of(16, 0));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(narrowSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		// Propose 09:00-09:30, within clinic hours but outside vet hours (10:00-16:00)
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.OUTSIDE_VET_HOURS)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.OUTSIDE_VET_HOURS).get(0).message())
			.contains("outside Dr. Carter's hours");
	}

	@Test
	void vetTimeOffConflict() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(true);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.VET_TIME_OFF)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.VET_TIME_OFF).get(0).message())
			.contains("Dr. Carter has time off on");
	}

	@Test
	void specialtyMismatchVetLacksRequiredSpecialty() {
		Specialty surgery = new Specialty();
		surgery.setId(2);
		surgery.setName("surgery");

		AppointmentType surgeryType = new AppointmentType();
		surgeryType.setName("Surgery");
		surgeryType.setDefaultDurationMinutes(90);
		surgeryType.setRequiredSpecialty(surgery);

		// vet (Carter) has no specialties
		stubAllClear();
		// Re-stub with surgeryType
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, surgeryType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.SPECIALTY_MISMATCH)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.SPECIALTY_MISMATCH).get(0).message())
			.contains("Surgery requires surgery specialty, but Dr. Carter does not have it");
	}

	@Test
	void specialtyMatchVetHasRequiredSpecialty() {
		Specialty surgery = new Specialty();
		surgery.setId(2);
		surgery.setName("surgery");

		AppointmentType surgeryType = new AppointmentType();
		surgeryType.setName("Surgery");
		surgeryType.setDefaultDurationMinutes(90);
		surgeryType.setRequiredSpecialty(surgery);

		// Give vet the surgery specialty
		Vet douglas = new Vet();
		douglas.setId(VET_ID);
		douglas.setFirstName("Linda");
		douglas.setLastName("Douglas");
		douglas.addSpecialty(surgery);

		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, surgeryType,
				douglas, null);

		assertThat(result.getConflictsByType(ConflictType.SPECIALTY_MISMATCH)).isEmpty();
	}

	@Test
	void noSpecialtyRequiredNoSpecialtyConflict() {
		// checkupType has null requiredSpecialty
		stubAllClear();

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.SPECIALTY_MISMATCH)).isEmpty();
	}

	@Test
	void vetOverlapDetected() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		Appointment overlap = makeAppointment(START, END, "Leo", "Carter");
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of(overlap));
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.VET_OVERLAP)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.VET_OVERLAP).get(0).message())
			.contains("Dr. Carter already has an appointment at 09:00-09:30 for Leo");
		assertThat(result.getConflictsByType(ConflictType.VET_OVERLAP).get(0).conflictingAppointment())
			.isSameAs(overlap);
	}

	@Test
	void petOverlapDetected() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		Appointment overlap = makeAppointment(START, END, "Leo", "Carter");
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of(overlap));
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.PET_OVERLAP)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.PET_OVERLAP).get(0).message())
			.contains("Leo already has an appointment at 09:00-09:30");
	}

	@Test
	void ownerOverlapDetectedForDifferentPet() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		// Owner has a different pet (id=2 "Max") with overlapping appointment
		Appointment overlap = makeAppointmentWithPetId(START, END, "Max", "Carter", 2);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of(overlap));

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.OWNER_OVERLAP)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.OWNER_OVERLAP).get(0).message())
			.contains("Owner already has an appointment for Max at 09:00-09:30");
	}

	@Test
	void ownerOverlapFiltersDuplicateSamePet() {
		// Pet PET_ID=1 has an overlap. Owner overlap query also returns the same overlap
		// (same pet id=1). It should NOT be double-reported as OWNER_OVERLAP.
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		Appointment overlap = makeAppointmentWithPetId(START, END, "Leo", "Carter", PET_ID);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of(overlap));
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of(overlap));

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		// PET_OVERLAP reported once, OWNER_OVERLAP NOT reported (same pet)
		assertThat(result.getConflictsByType(ConflictType.PET_OVERLAP)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.OWNER_OVERLAP)).isEmpty();
	}

	@Test
	void multipleConflictsReportedSimultaneously() {
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		Appointment vetOverlap = makeAppointment(START, END, "Leo", "Carter");
		Appointment petOverlap = makeAppointment(START, END, "Leo", "Carter");
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, null)).willReturn(List.of(vetOverlap));
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, null)).willReturn(List.of(petOverlap));
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				null);

		assertThat(result.getConflictsByType(ConflictType.VET_OVERLAP)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.PET_OVERLAP)).hasSize(1);
		assertThat(result.hasConflicts()).isTrue();
	}

	@Test
	void excludeSelfOnUpdatePreventsConflict() {
		Integer existingApptId = 5;
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		// excludeId=5 passed to all overlap queries, repo returns empty (no conflict)
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, END, existingApptId)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, END, existingApptId)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, END, existingApptId))
			.willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, END, checkupType, vet,
				existingApptId);

		assertThat(result.hasConflicts()).isFalse();
	}

	@Test
	void allConflictsReturnedAtOnce() {
		// Clinic open, but: end time after close, vet time off, specialty mismatch
		Specialty surgery = new Specialty();
		surgery.setId(2);
		surgery.setName("surgery");
		AppointmentType surgeryType = new AppointmentType();
		surgeryType.setName("Surgery");
		surgeryType.setDefaultDurationMinutes(90);
		surgeryType.setRequiredSpecialty(surgery);

		LocalTime lateEnd = LocalTime.of(17, 30);
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(true);
		given(appointmentRepo.findOverlappingByVet(VET_ID, MONDAY, START, lateEnd, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByPet(PET_ID, MONDAY, START, lateEnd, null)).willReturn(List.of());
		given(appointmentRepo.findOverlappingByOwner(OWNER_ID, MONDAY, START, lateEnd, null)).willReturn(List.of());

		ConflictResult result = service.detectConflicts(VET_ID, PET_ID, OWNER_ID, MONDAY, START, lateEnd, surgeryType,
				vet, null);

		assertThat(result.getConflictsByType(ConflictType.OUTSIDE_CLINIC_HOURS)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.VET_TIME_OFF)).hasSize(1);
		assertThat(result.getConflictsByType(ConflictType.SPECIALTY_MISMATCH)).hasSize(1);
		assertThat(result.hasConflicts()).isTrue();
	}

	// --- helper methods ---

	private Appointment makeAppointment(LocalTime start, LocalTime end, String petName, String vetLastName) {
		return makeAppointmentWithPetId(start, end, petName, vetLastName, PET_ID);
	}

	private Appointment makeAppointmentWithPetId(LocalTime start, LocalTime end, String petName, String vetLastName,
			Integer petId) {
		Appointment appt = new Appointment();
		appt.setStartTime(start);
		appt.setEndTime(end);

		org.springframework.samples.petclinic.owner.Pet pet = new org.springframework.samples.petclinic.owner.Pet();
		pet.setId(petId);
		pet.setName(petName);
		appt.setPet(pet);

		Vet v = new Vet();
		v.setLastName(vetLastName);
		appt.setVet(v);

		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(30);
		appt.setAppointmentType(type);

		return appt;
	}

}
