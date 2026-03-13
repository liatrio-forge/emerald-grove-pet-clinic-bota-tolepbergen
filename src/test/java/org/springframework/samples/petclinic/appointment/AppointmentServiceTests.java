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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

/**
 * Unit tests for {@link AppointmentService}. All dependencies are mocked with Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTests {

	@Mock
	private AppointmentRepository appointmentRepo;

	@Mock
	private AppointmentTypeRepository appointmentTypeRepo;

	@Mock
	private ConflictDetectionService conflictDetectionService;

	@Mock
	private OwnerRepository ownerRepo;

	@Mock
	private VetRepository vetRepo;

	@InjectMocks
	private AppointmentService appointmentService;

	private static final Integer PET_ID = 1;

	private static final Integer OWNER_ID = 1;

	private static final Integer VET_ID = 1;

	private static final Integer TYPE_ID = 1;

	private static final Integer APPOINTMENT_ID = 1;

	private static final LocalDate DATE = LocalDate.now().plusWeeks(2);

	private static final LocalTime START_TIME = LocalTime.of(9, 0);

	private Pet pet;

	private Owner owner;

	private Vet vet;

	private AppointmentType appointmentType;

	@BeforeEach
	void setUp() {
		// Build owner
		owner = new Owner();
		owner.setId(OWNER_ID);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");

		// Build pet and add to owner
		// Note: Owner.addPet() only adds isNew() pets (id == null).
		// Since our test pet has an id, we must add directly to the list.
		pet = new Pet();
		pet.setId(PET_ID);
		pet.setName("Leo");
		owner.getPets().add(pet);
		// Manually link pet back to owner for getOwner() access
		pet.setOwner(owner);

		// Build vet
		vet = new Vet();
		vet.setId(VET_ID);
		vet.setFirstName("James");
		vet.setLastName("Carter");

		// Build appointment type (30 min checkup)
		appointmentType = new AppointmentType();
		appointmentType.setId(TYPE_ID);
		appointmentType.setName("Checkup");
		appointmentType.setDefaultDurationMinutes(30);
	}

	// =========================================================================
	// createAppointment tests
	// =========================================================================

	@Test
	void successfulCreate() {
		// Arrange
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentTypeRepo.findByIdWithSpecialty(TYPE_ID)).willReturn(Optional.of(appointmentType));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		ConflictResult noConflicts = new ConflictResult(Collections.emptyList());
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(), any()))
			.willReturn(noConflicts);
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> {
			Appointment a = inv.getArgument(0);
			a.setId(APPOINTMENT_ID);
			a.setCreatedAt(LocalDateTime.now());
			return a;
		});

		// Act
		Appointment result = appointmentService.createAppointment(PET_ID, OWNER_ID, VET_ID, TYPE_ID, DATE, START_TIME,
				"Annual checkup");

		// Assert
		assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
		verify(appointmentRepo).save(any(Appointment.class));
	}

	@Test
	void endTimeCalculatedFor30MinType() {
		// Arrange
		appointmentType.setDefaultDurationMinutes(30);
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentTypeRepo.findByIdWithSpecialty(TYPE_ID)).willReturn(Optional.of(appointmentType));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		ConflictResult noConflicts = new ConflictResult(Collections.emptyList());
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(), any()))
			.willReturn(noConflicts);
		ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
		given(appointmentRepo.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

		// Act
		appointmentService.createAppointment(PET_ID, OWNER_ID, VET_ID, TYPE_ID, DATE, LocalTime.of(10, 0), null);

		// Assert
		assertThat(captor.getValue().getEndTime()).isEqualTo(LocalTime.of(10, 30));
	}

	@Test
	void endTimeCalculatedFor90MinType() {
		// Arrange
		appointmentType.setDefaultDurationMinutes(90);
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentTypeRepo.findByIdWithSpecialty(TYPE_ID)).willReturn(Optional.of(appointmentType));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		ConflictResult noConflicts = new ConflictResult(Collections.emptyList());
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(), any()))
			.willReturn(noConflicts);
		ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
		given(appointmentRepo.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

		// Act
		appointmentService.createAppointment(PET_ID, OWNER_ID, VET_ID, TYPE_ID, DATE, LocalTime.of(9, 0), null);

		// Assert
		assertThat(captor.getValue().getEndTime()).isEqualTo(LocalTime.of(10, 30));
	}

	@Test
	void conflictDetectedThrowsSchedulingConflictException() {
		// Arrange
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentTypeRepo.findByIdWithSpecialty(TYPE_ID)).willReturn(Optional.of(appointmentType));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		SchedulingConflict conflict = new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet has overlap", null);
		ConflictResult withConflicts = new ConflictResult(List.of(conflict));
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(), any()))
			.willReturn(withConflicts);

		// Act & Assert
		assertThatThrownBy(
				() -> appointmentService.createAppointment(PET_ID, OWNER_ID, VET_ID, TYPE_ID, DATE, START_TIME, null))
			.isInstanceOf(SchedulingConflictException.class)
			.satisfies(ex -> {
				SchedulingConflictException sce = (SchedulingConflictException) ex;
				assertThat(sce.getConflictResult().hasConflicts()).isTrue();
				assertThat(sce.getConflictResult().getConflictsByType(ConflictType.VET_OVERLAP)).hasSize(1);
			});
		verify(appointmentRepo, never()).save(any());
	}

	@Test
	void vetNotFoundThrowsResourceNotFoundException() {
		// Arrange
		given(vetRepo.findById(9999)).willReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(
				() -> appointmentService.createAppointment(PET_ID, OWNER_ID, 9999, TYPE_ID, DATE, START_TIME, null))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void appointmentTypeNotFoundThrowsResourceNotFoundException() {
		// Arrange
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentTypeRepo.findByIdWithSpecialty(9999)).willReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(
				() -> appointmentService.createAppointment(PET_ID, OWNER_ID, VET_ID, 9999, DATE, START_TIME, null))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void petNotBelongingToOwnerThrowsResourceNotFoundException() {
		// Arrange
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentTypeRepo.findByIdWithSpecialty(TYPE_ID)).willReturn(Optional.of(appointmentType));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));

		// Act & Assert: petId 9999 does not belong to owner — conflict detection must NOT
		// run
		assertThatThrownBy(
				() -> appointmentService.createAppointment(9999, OWNER_ID, VET_ID, TYPE_ID, DATE, START_TIME, null))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Pet not found");
		verify(conflictDetectionService, never()).detectConflicts(any(), any(), any(), any(), any(), any(), any(),
				any(), any());
	}

	@Test
	void ownerNotFoundThrowsResourceNotFoundException() {
		// Arrange
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentTypeRepo.findByIdWithSpecialty(TYPE_ID)).willReturn(Optional.of(appointmentType));
		given(ownerRepo.findById(9999)).willReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(
				() -> appointmentService.createAppointment(PET_ID, 9999, VET_ID, TYPE_ID, DATE, START_TIME, null))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	// =========================================================================
	// updateAppointment tests
	// =========================================================================

	@Test
	void successfulUpdateScheduledAppointment() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		ConflictResult noConflicts = new ConflictResult(Collections.emptyList());
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(),
				eq(APPOINTMENT_ID)))
			.willReturn(noConflicts);
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		Appointment result = appointmentService.updateAppointment(APPOINTMENT_ID, VET_ID, DATE, LocalTime.of(10, 0),
				"Updated");

		// Assert
		assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
		verify(conflictDetectionService).detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(),
				eq(APPOINTMENT_ID));
	}

	@Test
	void successfulUpdateConfirmedAppointment() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setStatus(AppointmentStatus.CONFIRMED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		ConflictResult noConflicts = new ConflictResult(Collections.emptyList());
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(),
				eq(APPOINTMENT_ID)))
			.willReturn(noConflicts);
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		LocalDate newDate = DATE.plusDays(1);
		Appointment result = appointmentService.updateAppointment(APPOINTMENT_ID, VET_ID, newDate, START_TIME, null);

		// Assert
		assertThat(result.getAppointmentDate()).isEqualTo(newDate);
	}

	@Test
	void updateCancelledAppointmentThrowsIllegalStateException() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setCancelledAt(LocalDateTime.now());
		// Manually set the status field to CANCELLED by bypassing state machine
		forceStatus(existing, AppointmentStatus.CANCELLED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));

		// Act & Assert
		assertThatThrownBy(() -> appointmentService.updateAppointment(APPOINTMENT_ID, VET_ID, DATE, START_TIME, null))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void updateCompletedAppointmentThrowsIllegalStateException() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		forceStatus(existing, AppointmentStatus.COMPLETED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));

		// Act & Assert
		assertThatThrownBy(() -> appointmentService.updateAppointment(APPOINTMENT_ID, VET_ID, DATE, START_TIME, null))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void updateWithConflictThrowsSchedulingConflictException() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		SchedulingConflict conflict = new SchedulingConflict(ConflictType.VET_OVERLAP, "Conflict", null);
		ConflictResult withConflicts = new ConflictResult(List.of(conflict));
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(),
				eq(APPOINTMENT_ID)))
			.willReturn(withConflicts);

		// Act & Assert
		assertThatThrownBy(() -> appointmentService.updateAppointment(APPOINTMENT_ID, VET_ID, DATE, START_TIME, null))
			.isInstanceOf(SchedulingConflictException.class);
	}

	@Test
	void updatePassesExcludeIdToConflictDetection() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		ConflictResult noConflicts = new ConflictResult(Collections.emptyList());
		given(conflictDetectionService.detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(),
				eq(APPOINTMENT_ID)))
			.willReturn(noConflicts);
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		appointmentService.updateAppointment(APPOINTMENT_ID, VET_ID, DATE, START_TIME, null);

		// Assert: conflict detection was called with excludeAppointmentId =
		// APPOINTMENT_ID
		verify(conflictDetectionService).detectConflicts(any(), any(), any(), any(), any(), any(), any(), any(),
				eq(APPOINTMENT_ID));
	}

	// =========================================================================
	// Status transition tests
	// =========================================================================

	@Test
	void confirmScheduledAppointment() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		Appointment result = appointmentService.confirmAppointment(APPOINTMENT_ID);

		// Assert
		assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
		verify(appointmentRepo).save(existing);
	}

	@Test
	void confirmConfirmedAppointmentThrowsIllegalStateException() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setStatus(AppointmentStatus.CONFIRMED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));

		// Act & Assert
		assertThatThrownBy(() -> appointmentService.confirmAppointment(APPOINTMENT_ID))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void cancelScheduledAppointment() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		Appointment result = appointmentService.cancelAppointment(APPOINTMENT_ID);

		// Assert
		assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
		assertThat(result.getCancelledAt()).isNotNull();
	}

	@Test
	void cancelConfirmedAppointment() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setStatus(AppointmentStatus.CONFIRMED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		Appointment result = appointmentService.cancelAppointment(APPOINTMENT_ID);

		// Assert
		assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
		assertThat(result.getCancelledAt()).isNotNull();
	}

	@Test
	void cancelCancelledAppointmentThrowsIllegalStateException() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		forceStatus(existing, AppointmentStatus.CANCELLED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));

		// Act & Assert
		assertThatThrownBy(() -> appointmentService.cancelAppointment(APPOINTMENT_ID))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void completeConfirmedAppointmentCreatesVisit() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setStatus(AppointmentStatus.CONFIRMED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));
		given(ownerRepo.save(any(Owner.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		Appointment result = appointmentService.completeAppointment(APPOINTMENT_ID);

		// Assert
		assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
		verify(ownerRepo).save(any(Owner.class));
	}

	@Test
	void completeScheduledAppointmentCreatesVisit() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));
		given(ownerRepo.save(any(Owner.class))).willAnswer(inv -> inv.getArgument(0));

		// Act
		Appointment result = appointmentService.completeAppointment(APPOINTMENT_ID);

		// Assert
		assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
		verify(ownerRepo).save(any(Owner.class));
	}

	@Test
	void completeCancelledAppointmentThrowsIllegalStateException() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		forceStatus(existing, AppointmentStatus.CANCELLED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));

		// Act & Assert
		assertThatThrownBy(() -> appointmentService.completeAppointment(APPOINTMENT_ID))
			.isInstanceOf(IllegalStateException.class);
		verify(ownerRepo, never()).save(any());
	}

	// =========================================================================
	// Visit creation tests
	// =========================================================================

	@Test
	void visitDateMatchesAppointmentDate() {
		// Arrange
		LocalDate appointmentDate = DATE.plusDays(3);
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setAppointmentDate(appointmentDate);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));
		ArgumentCaptor<Owner> ownerCaptor = ArgumentCaptor.forClass(Owner.class);
		given(ownerRepo.save(ownerCaptor.capture())).willAnswer(inv -> inv.getArgument(0));

		// Act
		appointmentService.completeAppointment(APPOINTMENT_ID);

		// Assert: the visit added to owner's pet has the correct date
		Pet savedPet = ownerCaptor.getValue().getPet(PET_ID);
		assertThat(savedPet.getVisits()).anySatisfy(v -> assertThat(v.getDate()).isEqualTo(appointmentDate));
	}

	@Test
	void visitDescriptionWithoutNotes() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setNotes(null);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));
		ArgumentCaptor<Owner> ownerCaptor = ArgumentCaptor.forClass(Owner.class);
		given(ownerRepo.save(ownerCaptor.capture())).willAnswer(inv -> inv.getArgument(0));

		// Act
		appointmentService.completeAppointment(APPOINTMENT_ID);

		// Assert
		Pet savedPet = ownerCaptor.getValue().getPet(PET_ID);
		assertThat(savedPet.getVisits())
			.anySatisfy(v -> assertThat(v.getDescription()).isEqualTo("Checkup with Dr. Carter"));
	}

	@Test
	void visitDescriptionWithNotes() {
		// Arrange
		AppointmentType surgeryType = new AppointmentType();
		surgeryType.setId(2);
		surgeryType.setName("Surgery");
		surgeryType.setDefaultDurationMinutes(90);

		Vet douglas = new Vet();
		douglas.setId(2);
		douglas.setFirstName("Linda");
		douglas.setLastName("Douglas");

		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		existing.setAppointmentType(surgeryType);
		existing.setVet(douglas);
		existing.setNotes("Spay surgery");

		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));
		ArgumentCaptor<Owner> ownerCaptor = ArgumentCaptor.forClass(Owner.class);
		given(ownerRepo.save(ownerCaptor.capture())).willAnswer(inv -> inv.getArgument(0));

		// Act
		appointmentService.completeAppointment(APPOINTMENT_ID);

		// Assert
		Pet savedPet = ownerCaptor.getValue().getPet(PET_ID);
		assertThat(savedPet.getVisits())
			.anySatisfy(v -> assertThat(v.getDescription()).isEqualTo("Surgery with Dr. Douglas - Spay surgery"));
	}

	@Test
	void visitLinkedToCorrectPet() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));
		given(ownerRepo.findById(OWNER_ID)).willReturn(Optional.of(owner));
		given(appointmentRepo.save(any(Appointment.class))).willAnswer(inv -> inv.getArgument(0));
		ArgumentCaptor<Owner> ownerCaptor = ArgumentCaptor.forClass(Owner.class);
		given(ownerRepo.save(ownerCaptor.capture())).willAnswer(inv -> inv.getArgument(0));

		// Act
		appointmentService.completeAppointment(APPOINTMENT_ID);

		// Assert: visit is linked to pet with id=PET_ID
		Pet savedPet = ownerCaptor.getValue().getPet(PET_ID);
		assertThat(savedPet).isNotNull();
		assertThat(savedPet.getVisits()).hasSize(1);
	}

	// =========================================================================
	// findById / findByDate / findByVetAndDateRange delegation tests
	// =========================================================================

	@Test
	void findByIdDelegatesToRepository() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByIdWithDetails(APPOINTMENT_ID)).willReturn(Optional.of(existing));

		// Act
		Optional<Appointment> result = appointmentService.findById(APPOINTMENT_ID);

		// Assert
		assertThat(result).contains(existing);
	}

	@Test
	void findByDateDelegatesToRepository() {
		// Arrange
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByDate(DATE)).willReturn(List.of(existing));

		// Act
		List<Appointment> result = appointmentService.findByDate(DATE);

		// Assert
		assertThat(result).containsExactly(existing);
	}

	@Test
	void findByVetAndDateRangeDelegatesToRepository() {
		// Arrange
		LocalDate end = DATE.plusDays(7);
		Appointment existing = buildAppointment(AppointmentStatus.SCHEDULED);
		given(appointmentRepo.findByVetIdAndDateBetween(VET_ID, DATE, end)).willReturn(List.of(existing));

		// Act
		List<Appointment> result = appointmentService.findByVetAndDateRange(VET_ID, DATE, end);

		// Assert
		assertThat(result).containsExactly(existing);
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	/**
	 * Builds a SCHEDULED appointment with vet, pet, type, and owner pre-linked.
	 */
	private Appointment buildAppointment(AppointmentStatus initialStatus) {
		Appointment a = new Appointment();
		a.setId(APPOINTMENT_ID);
		a.setAppointmentDate(DATE);
		a.setStartTime(START_TIME);
		a.setEndTime(START_TIME.plusMinutes(30));
		a.setPet(pet);
		a.setVet(vet);
		a.setAppointmentType(appointmentType);
		a.setCreatedAt(LocalDateTime.now());
		// status defaults to SCHEDULED; if we need a different initial state, use
		// forceStatus
		if (initialStatus != AppointmentStatus.SCHEDULED) {
			forceStatus(a, initialStatus);
		}
		return a;
	}

	/**
	 * Bypasses the state machine to force a specific status for testing terminal/invalid
	 * states.
	 */
	private void forceStatus(Appointment appointment, AppointmentStatus status) {
		// Use reflection to bypass the state machine in setStatus()
		try {
			java.lang.reflect.Field statusField = Appointment.class.getDeclaredField("status");
			statusField.setAccessible(true);
			statusField.set(appointment, status);
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Could not force status in test", e);
		}
	}

}
