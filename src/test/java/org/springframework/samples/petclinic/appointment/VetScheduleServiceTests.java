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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

/**
 * Unit tests for {@link VetScheduleService}. Uses Mockito for all dependencies.
 */
@ExtendWith(MockitoExtension.class)
class VetScheduleServiceTests {

	@Mock
	private VetScheduleRepository vetScheduleRepo;

	@Mock
	private VetTimeOffRepository vetTimeOffRepo;

	@Mock
	private ClinicScheduleConfigRepository clinicConfigRepo;

	@Mock
	private AppointmentRepository appointmentRepo;

	@Mock
	private VetRepository vetRepo;

	@InjectMocks
	private VetScheduleService vetScheduleService;

	private static final Integer VET_ID = 1;

	private static final LocalDate FUTURE_DATE = LocalDate.now().plusWeeks(4);

	private static final LocalDate PAST_DATE = LocalDate.of(2020, 1, 1);

	private Vet vet;

	private ClinicScheduleConfig mondayConfig;

	@BeforeEach
	void setUp() {
		vet = new Vet();
		vet.setId(VET_ID);
		vet.setFirstName("James");
		vet.setLastName("Carter");

		mondayConfig = new ClinicScheduleConfig();
		mondayConfig.setDayOfWeek(1);
		mondayConfig.setIsOpen(true);
		mondayConfig.setOpenTime(LocalTime.of(9, 0));
		mondayConfig.setCloseTime(LocalTime.of(17, 0));
		mondayConfig.setSlotDurationMinutes(30);
	}

	// --- getWeeklySchedule ---

	@Test
	void getScheduleForExistingVetReturnsSchedules() {
		VetSchedule s1 = new VetSchedule();
		s1.setDayOfWeek(1);
		VetSchedule s2 = new VetSchedule();
		s2.setDayOfWeek(2);
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(vetScheduleRepo.findByVetId(VET_ID)).willReturn(List.of(s1, s2));

		List<VetSchedule> result = vetScheduleService.getWeeklySchedule(VET_ID);

		assertThat(result).hasSize(2);
	}

	@Test
	void getScheduleForNonExistentVetThrowsResourceNotFoundException() {
		given(vetRepo.findById(9999)).willReturn(Optional.empty());

		assertThatThrownBy(() -> vetScheduleService.getWeeklySchedule(9999))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	// --- updateDaySchedule ---

	@Test
	void updateExistingDayScheduleUpdatesRecord() {
		VetSchedule existing = new VetSchedule();
		existing.setDayOfWeek(1);
		existing.setIsAvailable(true);
		existing.setStartTime(LocalTime.of(9, 0));
		existing.setEndTime(LocalTime.of(17, 0));
		existing.setVet(vet);

		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(existing));
		given(vetScheduleRepo.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		VetSchedule result = vetScheduleService.updateDaySchedule(VET_ID, 1, LocalTime.of(10, 0), LocalTime.of(16, 0),
				true);

		assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
		assertThat(result.getEndTime()).isEqualTo(LocalTime.of(16, 0));
	}

	@Test
	void createNewDayScheduleCreatesRecord() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(clinicConfigRepo.findByDayOfWeek(6)).willReturn(Optional.empty());
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 6)).willReturn(Optional.empty());
		given(vetScheduleRepo.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		VetSchedule result = vetScheduleService.updateDaySchedule(VET_ID, 6, LocalTime.of(9, 0), LocalTime.of(13, 0),
				false);

		assertThat(result.getDayOfWeek()).isEqualTo(6);
		assertThat(result.getIsAvailable()).isFalse();
	}

	@Test
	void startAfterEndThrowsIllegalArgumentException() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));

		assertThatThrownBy(
				() -> vetScheduleService.updateDaySchedule(VET_ID, 1, LocalTime.of(17, 0), LocalTime.of(9, 0), true))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Start time must be before end time");
	}

	@Test
	void startEqualsEndThrowsIllegalArgumentException() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));

		assertThatThrownBy(
				() -> vetScheduleService.updateDaySchedule(VET_ID, 1, LocalTime.of(9, 0), LocalTime.of(9, 0), true))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void outsideClinicHoursStartBeforeOpenThrowsException() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));

		assertThatThrownBy(
				() -> vetScheduleService.updateDaySchedule(VET_ID, 1, LocalTime.of(8, 0), LocalTime.of(16, 0), true))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("must fall within clinic hours");
	}

	@Test
	void endAfterClinicCloseThrowsException() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));

		assertThatThrownBy(
				() -> vetScheduleService.updateDaySchedule(VET_ID, 1, LocalTime.of(9, 0), LocalTime.of(18, 0), true))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("must fall within clinic hours");
	}

	@Test
	void scheduleOnClosedDayWithAvailableTrueThrowsException() {
		ClinicScheduleConfig sundayConfig = new ClinicScheduleConfig();
		sundayConfig.setDayOfWeek(7);
		sundayConfig.setIsOpen(false);
		sundayConfig.setOpenTime(LocalTime.of(9, 0));
		sundayConfig.setCloseTime(LocalTime.of(17, 0));
		sundayConfig.setSlotDurationMinutes(30);

		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(sundayConfig));

		assertThatThrownBy(
				() -> vetScheduleService.updateDaySchedule(VET_ID, 7, LocalTime.of(9, 0), LocalTime.of(13, 0), true))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Cannot schedule vet on a day when the clinic is closed");
	}

	@Test
	void setUnavailableOnClosedDayIsAllowed() {
		ClinicScheduleConfig sundayConfig = new ClinicScheduleConfig();
		sundayConfig.setDayOfWeek(7);
		sundayConfig.setIsOpen(false);
		sundayConfig.setOpenTime(LocalTime.of(9, 0));
		sundayConfig.setCloseTime(LocalTime.of(17, 0));
		sundayConfig.setSlotDurationMinutes(30);

		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(sundayConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 7)).willReturn(Optional.empty());
		given(vetScheduleRepo.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		// isAvailable=false on closed day should succeed
		VetSchedule result = vetScheduleService.updateDaySchedule(VET_ID, 7, LocalTime.of(9, 0), LocalTime.of(13, 0),
				false);

		assertThat(result).isNotNull();
		assertThat(result.getIsAvailable()).isFalse();
	}

	@Test
	void withinClinicHoursSucceeds() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.empty());
		given(vetScheduleRepo.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		VetSchedule result = vetScheduleService.updateDaySchedule(VET_ID, 1, LocalTime.of(10, 0), LocalTime.of(15, 0),
				true);

		assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
	}

	// --- addTimeOff ---

	@Test
	void addTimeOffNoAppointmentsReturnsEmptyWarning() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentRepo.findByVetIdAndDate(VET_ID, FUTURE_DATE)).willReturn(List.of());
		VetTimeOff saved = new VetTimeOff();
		saved.setDate(FUTURE_DATE);
		saved.setReason("Vacation");
		given(vetTimeOffRepo.save(any())).willReturn(saved);

		TimeOffResult result = vetScheduleService.addTimeOff(VET_ID, FUTURE_DATE, "Vacation");

		assertThat(result.hasExistingAppointments()).isFalse();
		assertThat(result.timeOff().getDate()).isEqualTo(FUTURE_DATE);
		assertThat(result.timeOff().getReason()).isEqualTo("Vacation");
	}

	@Test
	void addTimeOffWithAppointmentsReturnsWarnings() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		Appointment a1 = new Appointment();
		Appointment a2 = new Appointment();
		given(appointmentRepo.findByVetIdAndDate(VET_ID, FUTURE_DATE)).willReturn(List.of(a1, a2));
		given(vetTimeOffRepo.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		TimeOffResult result = vetScheduleService.addTimeOff(VET_ID, FUTURE_DATE, "Emergency");

		assertThat(result.hasExistingAppointments()).isTrue();
		assertThat(result.existingAppointments()).hasSize(2);
	}

	@Test
	void addTimeOffPastDateThrowsIllegalArgumentException() {
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));

		assertThatThrownBy(() -> vetScheduleService.addTimeOff(VET_ID, PAST_DATE, "Past"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Cannot add time off for past dates");
	}

	@Test
	void addTimeOffTodayIsAllowed() {
		LocalDate today = LocalDate.now();
		given(vetRepo.findById(VET_ID)).willReturn(Optional.of(vet));
		given(appointmentRepo.findByVetIdAndDate(VET_ID, today)).willReturn(List.of());
		given(vetTimeOffRepo.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		TimeOffResult result = vetScheduleService.addTimeOff(VET_ID, today, "Sick day");

		assertThat(result).isNotNull();
	}

	@Test
	void addTimeOffNonExistentVetThrowsResourceNotFoundException() {
		given(vetRepo.findById(9999)).willReturn(Optional.empty());

		assertThatThrownBy(() -> vetScheduleService.addTimeOff(9999, FUTURE_DATE, "Test"))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	// --- removeTimeOff ---

	@Test
	void removeExistingTimeOffDeletesIt() {
		Vet vet = new Vet();
		vet.setId(1);
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setId(1);
		timeOff.setVet(vet);
		given(vetTimeOffRepo.findById(1)).willReturn(Optional.of(timeOff));

		vetScheduleService.removeTimeOff(1, 1);

		verify(vetTimeOffRepo).delete(timeOff);
	}

	@Test
	void removeTimeOffWrongVetThrowsResourceNotFoundException() {
		Vet vet = new Vet();
		vet.setId(2);
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setId(1);
		timeOff.setVet(vet);
		given(vetTimeOffRepo.findById(1)).willReturn(Optional.of(timeOff));

		assertThatThrownBy(() -> vetScheduleService.removeTimeOff(99, 1)).isInstanceOf(ResourceNotFoundException.class);
		verify(vetTimeOffRepo, never()).delete(any());
	}

	@Test
	void removeNonExistentTimeOffThrowsResourceNotFoundException() {
		given(vetTimeOffRepo.findById(9999)).willReturn(Optional.empty());

		assertThatThrownBy(() -> vetScheduleService.removeTimeOff(1, 9999))
			.isInstanceOf(ResourceNotFoundException.class);
		verify(vetTimeOffRepo, never()).delete(any());
	}

}
