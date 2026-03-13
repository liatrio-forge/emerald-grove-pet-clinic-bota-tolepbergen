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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for computing available time slots and available vets on a given date.
 *
 * <p>
 * All methods are read-only transactions. Availability is determined by combining clinic
 * operating hours, vet weekly schedules, vet time-off entries, and existing non-cancelled
 * appointments.
 */
@Service
@Transactional(readOnly = true)
public class AvailabilityService {

	private final ClinicScheduleConfigRepository clinicConfigRepo;

	private final VetScheduleRepository vetScheduleRepo;

	private final VetTimeOffRepository vetTimeOffRepo;

	private final AppointmentRepository appointmentRepo;

	public AvailabilityService(ClinicScheduleConfigRepository clinicConfigRepo, VetScheduleRepository vetScheduleRepo,
			VetTimeOffRepository vetTimeOffRepo, AppointmentRepository appointmentRepo) {
		this.clinicConfigRepo = clinicConfigRepo;
		this.vetScheduleRepo = vetScheduleRepo;
		this.vetTimeOffRepo = vetTimeOffRepo;
		this.appointmentRepo = appointmentRepo;
	}

	/**
	 * Returns available time slots for a vet on a date given a desired appointment
	 * duration.
	 *
	 * <p>
	 * Returns an empty list if the clinic is closed, vet doesn't work that day, or vet
	 * has time off.
	 * @param vetId the vet's ID
	 * @param date the desired date
	 * @param durationMinutes the desired appointment duration in minutes
	 * @return list of available {@link TimeSlot}s, possibly empty
	 */
	public List<TimeSlot> getAvailableSlots(Integer vetId, LocalDate date, int durationMinutes) {
		int dayOfWeek = date.getDayOfWeek().getValue();

		// 1. Check clinic is open
		Optional<ClinicScheduleConfig> clinicConfigOpt = clinicConfigRepo.findByDayOfWeek(dayOfWeek);
		if (clinicConfigOpt.isEmpty() || !Boolean.TRUE.equals(clinicConfigOpt.get().getIsOpen())) {
			return List.of();
		}
		ClinicScheduleConfig clinicConfig = clinicConfigOpt.get();

		// 2. Get vet schedule for this day
		Optional<VetSchedule> vetScheduleOpt = vetScheduleRepo.findByVetIdAndDayOfWeek(vetId, dayOfWeek);
		if (vetScheduleOpt.isEmpty() || !Boolean.TRUE.equals(vetScheduleOpt.get().getIsAvailable())) {
			return List.of();
		}
		VetSchedule vetSchedule = vetScheduleOpt.get();

		// 3. Check vet time off
		if (vetTimeOffRepo.existsByVetIdAndDate(vetId, date)) {
			return List.of();
		}

		// 4. Determine effective working window (intersection of clinic and vet hours)
		LocalTime effectiveStart = clinicConfig.getOpenTime().isAfter(vetSchedule.getStartTime())
				? clinicConfig.getOpenTime() : vetSchedule.getStartTime();
		LocalTime effectiveEnd = clinicConfig.getCloseTime().isBefore(vetSchedule.getEndTime())
				? clinicConfig.getCloseTime() : vetSchedule.getEndTime();

		if (!effectiveStart.isBefore(effectiveEnd)) {
			return List.of();
		}

		// 5. Get slot duration from clinic config
		int slotDurationMinutes = clinicConfig.getSlotDurationMinutes();

		// 6. Get existing appointments for this vet on this date
		List<Appointment> existingAppointments = appointmentRepo.findByVetIdAndDate(vetId, date);

		// 7. Generate candidate slots and filter out occupied ones
		List<TimeSlot> availableSlots = new ArrayList<>();
		LocalTime slotStart = effectiveStart;

		while (slotStart.plusMinutes(durationMinutes).compareTo(effectiveEnd) <= 0) {
			LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);
			TimeSlot candidate = new TimeSlot(slotStart, slotEnd);

			// Check if this candidate overlaps with any existing appointment
			boolean occupied = existingAppointments.stream()
				.anyMatch(appt -> candidate.overlaps(appt.getStartTime(), appt.getEndTime()));

			if (!occupied) {
				availableSlots.add(candidate);
			}

			slotStart = slotStart.plusMinutes(slotDurationMinutes);
		}

		return availableSlots;
	}

	/**
	 * Returns list of vets available on a given date, optionally filtered by specialty.
	 *
	 * <p>
	 * A vet is "available" if: the clinic is open that day, the vet is scheduled to work
	 * that day, and the vet has no time-off entry for that date.
	 * @param date the desired date
	 * @param specialtyId optional specialty filter; pass {@code null} to skip filtering
	 * @return list of available {@link Vet}s, possibly empty
	 */
	public List<Vet> getAvailableVets(LocalDate date, Integer specialtyId) {
		int dayOfWeek = date.getDayOfWeek().getValue();

		// 1. Check clinic is open on this date
		Optional<ClinicScheduleConfig> clinicConfigOpt = clinicConfigRepo.findByDayOfWeek(dayOfWeek);
		if (clinicConfigOpt.isEmpty() || !Boolean.TRUE.equals(clinicConfigOpt.get().getIsOpen())) {
			return List.of();
		}

		// 2. Get all vet schedules for this day of week where vet is available
		List<VetSchedule> availableSchedules = vetScheduleRepo.findAvailableByDayOfWeek(dayOfWeek);

		// 3. Get vets with time off on this date
		List<VetTimeOff> timeOffList = vetTimeOffRepo.findByDate(date);
		Set<Integer> vetsWithTimeOff = timeOffList.stream()
			.map(vto -> vto.getVet().getId())
			.collect(Collectors.toSet());

		// 4. Filter schedules: remove vets with time off, apply optional specialty filter
		return availableSchedules.stream()
			.filter(schedule -> !vetsWithTimeOff.contains(schedule.getVet().getId()))
			.map(VetSchedule::getVet)
			.filter(vet -> specialtyId == null
					|| vet.getSpecialties().stream().anyMatch(specialty -> specialtyId.equals(specialty.getId())))
			.collect(Collectors.toList());
	}

}
