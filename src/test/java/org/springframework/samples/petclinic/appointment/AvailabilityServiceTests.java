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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

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
 * Unit tests for {@link AvailabilityService}. Uses Mockito to isolate from database. 100%
 * branch coverage required.
 */
@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTests {

	@Mock
	private ClinicScheduleConfigRepository clinicConfigRepo;

	@Mock
	private VetScheduleRepository vetScheduleRepo;

	@Mock
	private VetTimeOffRepository vetTimeOffRepo;

	@Mock
	private AppointmentRepository appointmentRepo;

	@InjectMocks
	private AvailabilityService availabilityService;

	// Monday = ISO day 1
	private static final LocalDate MONDAY = LocalDate.of(2026, 4, 6);

	// Sunday = ISO day 7
	private static final LocalDate SUNDAY = LocalDate.of(2026, 4, 5);

	private static final int VET_ID = 1;

	private ClinicScheduleConfig openClinicConfig;

	private VetSchedule availableVetSchedule;

	@BeforeEach
	void setUp() {
		openClinicConfig = new ClinicScheduleConfig();
		openClinicConfig.setDayOfWeek(1); // Monday
		openClinicConfig.setIsOpen(true);
		openClinicConfig.setOpenTime(LocalTime.of(9, 0));
		openClinicConfig.setCloseTime(LocalTime.of(17, 0));
		openClinicConfig.setSlotDurationMinutes(30);

		availableVetSchedule = new VetSchedule();
		availableVetSchedule.setDayOfWeek(1);
		availableVetSchedule.setIsAvailable(true);
		availableVetSchedule.setStartTime(LocalTime.of(9, 0));
		availableVetSchedule.setEndTime(LocalTime.of(17, 0));
		Vet vet = new Vet();
		availableVetSchedule.setVet(vet);
	}

	// --- getAvailableSlots ---

	@Test
	void clinicClosedDayReturnsEmptySlots() {
		// Arrange: clinic config present but closed
		ClinicScheduleConfig closedConfig = new ClinicScheduleConfig();
		closedConfig.setDayOfWeek(7);
		closedConfig.setIsOpen(false);
		closedConfig.setOpenTime(LocalTime.of(9, 0));
		closedConfig.setCloseTime(LocalTime.of(17, 0));
		closedConfig.setSlotDurationMinutes(30);
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(closedConfig));

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, SUNDAY, 30);

		// Assert
		assertThat(slots).isEmpty();
	}

	@Test
	void clinicConfigMissingReturnsEmptySlots() {
		// Arrange: no config for this day
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.empty());

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, SUNDAY, 30);

		// Assert
		assertThat(slots).isEmpty();
	}

	@Test
	void vetNotScheduledReturnsEmptySlots() {
		// Arrange: clinic open, but no vet schedule for this day
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.empty());

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 30);

		// Assert
		assertThat(slots).isEmpty();
	}

	@Test
	void vetNotAvailableReturnsEmptySlots() {
		// Arrange: clinic open, vet has schedule but isAvailable=false
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		VetSchedule unavailableSchedule = new VetSchedule();
		unavailableSchedule.setDayOfWeek(1);
		unavailableSchedule.setIsAvailable(false);
		unavailableSchedule.setStartTime(LocalTime.of(9, 0));
		unavailableSchedule.setEndTime(LocalTime.of(17, 0));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(unavailableSchedule));

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 30);

		// Assert
		assertThat(slots).isEmpty();
	}

	@Test
	void vetHasTimeOffReturnsEmptySlots() {
		// Arrange: clinic open, vet available, but has time off
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableVetSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(true);

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 30);

		// Assert
		assertThat(slots).isEmpty();
	}

	@Test
	void emptyDayReturns16Slots() {
		// Arrange: clinic 09:00-17:00, 30-min slots, vet 09:00-17:00, no appointments
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableVetSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findByVetIdAndDate(VET_ID, MONDAY)).willReturn(List.of());

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 30);

		// Assert: 09:00 to 16:30 = 16 slots of 30 min each
		assertThat(slots).hasSize(16);
		assertThat(slots.get(0).startTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(slots.get(0).endTime()).isEqualTo(LocalTime.of(9, 30));
		assertThat(slots.get(15).startTime()).isEqualTo(LocalTime.of(16, 30));
		assertThat(slots.get(15).endTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	void partiallyBookedReturnsOnlyFreeSlots() {
		// Arrange: one appointment from 09:00-09:30
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableVetSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		Appointment existing = new Appointment();
		existing.setStartTime(LocalTime.of(9, 0));
		existing.setEndTime(LocalTime.of(9, 30));
		given(appointmentRepo.findByVetIdAndDate(VET_ID, MONDAY)).willReturn(List.of(existing));

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 30);

		// Assert: 15 slots (09:00 excluded)
		assertThat(slots).hasSize(15);
		assertThat(slots.stream().noneMatch(s -> s.startTime().equals(LocalTime.of(9, 0)))).isTrue();
	}

	@Test
	void fullyBookedReturnsEmptySlots() {
		// Arrange: one big appointment covering entire day
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableVetSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		Appointment fullDay = new Appointment();
		fullDay.setStartTime(LocalTime.of(9, 0));
		fullDay.setEndTime(LocalTime.of(17, 0));
		given(appointmentRepo.findByVetIdAndDate(VET_ID, MONDAY)).willReturn(List.of(fullDay));

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 30);

		// Assert
		assertThat(slots).isEmpty();
	}

	@Test
	void multiSlotAppointmentExcludesOverlappingStartTimes() {
		// Arrange: 90-min duration. Existing appointment 09:30-10:00.
		// Slot starting at 09:00 would need 09:00-10:30, overlaps with 09:30-10:00 =>
		// excluded
		// Slot starting at 10:00 needs 10:00-11:30, no overlap => included
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableVetSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);

		Appointment existing = new Appointment();
		existing.setStartTime(LocalTime.of(9, 30));
		existing.setEndTime(LocalTime.of(10, 0));
		given(appointmentRepo.findByVetIdAndDate(VET_ID, MONDAY)).willReturn(List.of(existing));

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 90);

		// Assert: 09:00 slot excluded (overlaps), 10:00 slot included
		assertThat(slots.stream().noneMatch(s -> s.startTime().equals(LocalTime.of(9, 0)))).isTrue();
		assertThat(slots.stream().anyMatch(s -> s.startTime().equals(LocalTime.of(10, 0)))).isTrue();
	}

	@Test
	void vetHoursNarrowerThanClinicUsesVetHours() {
		// Arrange: clinic 09:00-17:00, vet 10:00-16:00
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		VetSchedule narrowSchedule = new VetSchedule();
		narrowSchedule.setDayOfWeek(1);
		narrowSchedule.setIsAvailable(true);
		narrowSchedule.setStartTime(LocalTime.of(10, 0));
		narrowSchedule.setEndTime(LocalTime.of(16, 0));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(narrowSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findByVetIdAndDate(VET_ID, MONDAY)).willReturn(List.of());

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 30);

		// Assert: 10:00-15:30 = 12 slots
		assertThat(slots).hasSize(12);
		assertThat(slots.get(0).startTime()).isEqualTo(LocalTime.of(10, 0));
		assertThat(slots.get(slots.size() - 1).startTime()).isEqualTo(LocalTime.of(15, 30));
	}

	@Test
	void clinicHoursNarrowerThanVetUsesClinicHours() {
		// Arrange: clinic closes at 13:00 (Saturday), vet schedule 09:00-17:00
		ClinicScheduleConfig saturdayConfig = new ClinicScheduleConfig();
		saturdayConfig.setDayOfWeek(6);
		saturdayConfig.setIsOpen(true);
		saturdayConfig.setOpenTime(LocalTime.of(9, 0));
		saturdayConfig.setCloseTime(LocalTime.of(13, 0));
		saturdayConfig.setSlotDurationMinutes(30);

		VetSchedule satSchedule = new VetSchedule();
		satSchedule.setDayOfWeek(6);
		satSchedule.setIsAvailable(true);
		satSchedule.setStartTime(LocalTime.of(9, 0));
		satSchedule.setEndTime(LocalTime.of(17, 0));

		LocalDate saturday = LocalDate.of(2026, 4, 11); // Saturday
		given(clinicConfigRepo.findByDayOfWeek(6)).willReturn(Optional.of(saturdayConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 6)).willReturn(Optional.of(satSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, saturday)).willReturn(false);
		given(appointmentRepo.findByVetIdAndDate(VET_ID, saturday)).willReturn(List.of());

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, saturday, 30);

		// Assert: 09:00-12:30 = 8 slots
		assertThat(slots).hasSize(8);
		assertThat(slots.get(slots.size() - 1).startTime()).isEqualTo(LocalTime.of(12, 30));
		assertThat(slots.stream().noneMatch(s -> s.startTime().compareTo(LocalTime.of(13, 0)) >= 0)).isTrue();
	}

	@Test
	void durationExceedsRemainingTimeExcludesLateSlots() {
		// Arrange: clinic 09:00-17:00, 30-min slot grid, 90-min duration
		// Last valid start = 15:30 (ends at 17:00); 16:00 and 16:30 are NOT returned
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));
		given(vetScheduleRepo.findByVetIdAndDayOfWeek(VET_ID, 1)).willReturn(Optional.of(availableVetSchedule));
		given(vetTimeOffRepo.existsByVetIdAndDate(VET_ID, MONDAY)).willReturn(false);
		given(appointmentRepo.findByVetIdAndDate(VET_ID, MONDAY)).willReturn(List.of());

		// Act
		List<TimeSlot> slots = availabilityService.getAvailableSlots(VET_ID, MONDAY, 90);

		// Assert: 09:00 to 15:30 = 14 slots
		assertThat(slots).hasSize(14);
		assertThat(slots.get(slots.size() - 1).startTime()).isEqualTo(LocalTime.of(15, 30));
		assertThat(slots.stream().noneMatch(s -> s.startTime().equals(LocalTime.of(16, 0)))).isTrue();
		assertThat(slots.stream().noneMatch(s -> s.startTime().equals(LocalTime.of(16, 30)))).isTrue();
	}

	// --- getAvailableVets ---

	@Test
	void clinicClosedReturnsEmptyVets() {
		// Arrange: Sunday, clinic closed
		ClinicScheduleConfig closedConfig = new ClinicScheduleConfig();
		closedConfig.setDayOfWeek(7);
		closedConfig.setIsOpen(false);
		closedConfig.setOpenTime(LocalTime.of(9, 0));
		closedConfig.setCloseTime(LocalTime.of(17, 0));
		closedConfig.setSlotDurationMinutes(30);
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(closedConfig));

		// Act
		List<Vet> vets = availabilityService.getAvailableVets(SUNDAY, null);

		// Assert
		assertThat(vets).isEmpty();
	}

	@Test
	void clinicConfigMissingReturnsEmptyVets() {
		// Arrange: no config for this day
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.empty());

		// Act
		List<Vet> vets = availabilityService.getAvailableVets(SUNDAY, null);

		// Assert
		assertThat(vets).isEmpty();
	}

	@Test
	void allVetsAvailableOnNormalWeekday() {
		// Arrange: clinic open, 3 vets scheduled, none have time off
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));

		Vet vet1 = makeVet(1, "Carter");
		Vet vet2 = makeVet(2, "Leary");
		Vet vet3 = makeVet(3, "Douglas");

		VetSchedule s1 = makeSchedule(1, vet1);
		VetSchedule s2 = makeSchedule(1, vet2);
		VetSchedule s3 = makeSchedule(1, vet3);
		given(vetScheduleRepo.findAvailableByDayOfWeek(1)).willReturn(List.of(s1, s2, s3));
		given(vetTimeOffRepo.findByDate(MONDAY)).willReturn(List.of());

		// Act
		List<Vet> vets = availabilityService.getAvailableVets(MONDAY, null);

		// Assert
		assertThat(vets).hasSize(3);
	}

	@Test
	void someVetsOnTimeOffAreExcluded() {
		// Arrange: clinic open, 3 vets scheduled, 1 has time off
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));

		Vet vet1 = makeVet(1, "Carter");
		Vet vet2 = makeVet(2, "Leary");
		Vet vet3 = makeVet(3, "Douglas");

		VetSchedule s1 = makeSchedule(1, vet1);
		VetSchedule s2 = makeSchedule(1, vet2);
		VetSchedule s3 = makeSchedule(1, vet3);
		given(vetScheduleRepo.findAvailableByDayOfWeek(1)).willReturn(List.of(s1, s2, s3));

		// vet2 has time off
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setVet(vet2);
		given(vetTimeOffRepo.findByDate(MONDAY)).willReturn(List.of(timeOff));

		// Act
		List<Vet> vets = availabilityService.getAvailableVets(MONDAY, null);

		// Assert: 2 vets returned, vet2 excluded
		assertThat(vets).hasSize(2);
		assertThat(vets.stream().noneMatch(v -> v.getLastName().equals("Leary"))).isTrue();
	}

	@Test
	void filterBySpecialtyReturnsOnlyMatchingVets() {
		// Arrange: 2 vets, only one has surgery specialty (id=2)
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));

		Specialty surgery = new Specialty();
		surgery.setId(2);
		surgery.setName("surgery");

		Vet vet1 = makeVet(1, "Carter");
		Vet vet2 = makeVet(2, "Douglas");
		vet2.addSpecialty(surgery);

		VetSchedule s1 = makeSchedule(1, vet1);
		VetSchedule s2 = makeSchedule(1, vet2);
		given(vetScheduleRepo.findAvailableByDayOfWeek(1)).willReturn(List.of(s1, s2));
		given(vetTimeOffRepo.findByDate(MONDAY)).willReturn(List.of());

		// Act
		List<Vet> vets = availabilityService.getAvailableVets(MONDAY, 2);

		// Assert: only Douglas returned
		assertThat(vets).hasSize(1);
		assertThat(vets.get(0).getLastName()).isEqualTo("Douglas");
	}

	@Test
	void noSpecialtyFilterReturnsAllAvailableVets() {
		// Arrange: 2 vets with different specialties, specialtyId=null
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));

		Vet vet1 = makeVet(1, "Carter");
		Vet vet2 = makeVet(2, "Douglas");

		VetSchedule s1 = makeSchedule(1, vet1);
		VetSchedule s2 = makeSchedule(1, vet2);
		given(vetScheduleRepo.findAvailableByDayOfWeek(1)).willReturn(List.of(s1, s2));
		given(vetTimeOffRepo.findByDate(MONDAY)).willReturn(List.of());

		// Act
		List<Vet> vets = availabilityService.getAvailableVets(MONDAY, null);

		// Assert: both returned
		assertThat(vets).hasSize(2);
	}

	@Test
	void noVetsWithRequiredSpecialtyReturnsEmpty() {
		// Arrange: 2 vets, neither has specialtyId=99
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(openClinicConfig));

		Vet vet1 = makeVet(1, "Carter");
		Vet vet2 = makeVet(2, "Douglas");

		VetSchedule s1 = makeSchedule(1, vet1);
		VetSchedule s2 = makeSchedule(1, vet2);
		given(vetScheduleRepo.findAvailableByDayOfWeek(1)).willReturn(List.of(s1, s2));
		given(vetTimeOffRepo.findByDate(MONDAY)).willReturn(List.of());

		// Act
		List<Vet> vets = availabilityService.getAvailableVets(MONDAY, 99);

		// Assert
		assertThat(vets).isEmpty();
	}

	// --- helpers ---

	private Vet makeVet(int id, String lastName) {
		Vet vet = new Vet();
		vet.setId(id);
		vet.setFirstName("Dr.");
		vet.setLastName(lastName);
		return vet;
	}

	private VetSchedule makeSchedule(int dayOfWeek, Vet vet) {
		VetSchedule schedule = new VetSchedule();
		schedule.setDayOfWeek(dayOfWeek);
		schedule.setIsAvailable(true);
		schedule.setStartTime(LocalTime.of(9, 0));
		schedule.setEndTime(LocalTime.of(17, 0));
		schedule.setVet(vet);
		return schedule;
	}

}
