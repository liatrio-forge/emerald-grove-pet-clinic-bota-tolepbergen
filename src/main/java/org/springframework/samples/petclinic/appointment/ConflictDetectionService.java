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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that validates a proposed appointment against all scheduling conflict rules and
 * returns a {@link ConflictResult} describing every conflict found.
 *
 * <p>
 * All checks run regardless of earlier failures so that the caller receives a complete
 * picture of all issues to resolve. Exception: if the clinic is closed, the method
 * returns early since further checks are meaningless.
 *
 * <p>
 * 100% branch coverage required by the project TDD standards.
 */
@Service
@Transactional(readOnly = true)
public class ConflictDetectionService {

	private final AppointmentRepository appointmentRepo;

	private final ClinicScheduleConfigRepository clinicConfigRepo;

	private final VetScheduleRepository vetScheduleRepo;

	private final VetTimeOffRepository vetTimeOffRepo;

	public ConflictDetectionService(AppointmentRepository appointmentRepo,
			ClinicScheduleConfigRepository clinicConfigRepo, VetScheduleRepository vetScheduleRepo,
			VetTimeOffRepository vetTimeOffRepo) {
		this.appointmentRepo = appointmentRepo;
		this.clinicConfigRepo = clinicConfigRepo;
		this.vetScheduleRepo = vetScheduleRepo;
		this.vetTimeOffRepo = vetTimeOffRepo;
	}

	/**
	 * Validates a proposed appointment against all conflict rules.
	 *
	 * <p>
	 * Runs all applicable checks and collects every conflict. The clinic-closed check
	 * causes an early return since all other checks are moot when the clinic is closed.
	 * @param vetId the proposed vet's ID
	 * @param petId the proposed pet's ID
	 * @param ownerId the pet's owner ID
	 * @param date the proposed date
	 * @param startTime the proposed start time
	 * @param endTime the proposed end time
	 * @param appointmentType the proposed appointment type (for specialty check)
	 * @param vet the proposed vet entity (for specialty check; must have specialties
	 * loaded)
	 * @param excludeAppointmentId appointment ID to exclude during update checks; pass
	 * {@code null} for new appointments
	 * @return a {@link ConflictResult} containing all detected conflicts
	 */
	public ConflictResult detectConflicts(Integer vetId, Integer petId, Integer ownerId, LocalDate date,
			LocalTime startTime, LocalTime endTime, AppointmentType appointmentType, Vet vet,
			Integer excludeAppointmentId) {

		List<SchedulingConflict> conflicts = new ArrayList<>();
		int dayOfWeek = date.getDayOfWeek().getValue();
		String dayName = DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

		// Rule 1: Clinic closed check (early return)
		Optional<ClinicScheduleConfig> clinicConfigOpt = clinicConfigRepo.findByDayOfWeek(dayOfWeek);
		if (clinicConfigOpt.isEmpty() || !Boolean.TRUE.equals(clinicConfigOpt.get().getIsOpen())) {
			conflicts.add(new SchedulingConflict(ConflictType.CLINIC_CLOSED, "Clinic is closed on " + dayName, null));
			return new ConflictResult(conflicts);
		}
		ClinicScheduleConfig clinicConfig = clinicConfigOpt.get();

		// Rule 2: Outside clinic hours
		if (startTime.isBefore(clinicConfig.getOpenTime()) || endTime.isAfter(clinicConfig.getCloseTime())) {
			conflicts.add(new SchedulingConflict(ConflictType.OUTSIDE_CLINIC_HOURS,
					"Appointment time " + startTime + "-" + endTime + " is outside clinic hours "
							+ clinicConfig.getOpenTime() + "-" + clinicConfig.getCloseTime(),
					null));
		}

		// Rule 3: Vet not available
		Optional<VetSchedule> vetScheduleOpt = vetScheduleRepo.findByVetIdAndDayOfWeek(vetId, dayOfWeek);
		if (vetScheduleOpt.isEmpty() || !Boolean.TRUE.equals(vetScheduleOpt.get().getIsAvailable())) {
			String vetName = vet != null ? "Dr. " + vet.getLastName() : "Vet";
			conflicts.add(new SchedulingConflict(ConflictType.VET_NOT_AVAILABLE,
					vetName + " does not work on " + dayName, null));
		}
		else {
			// Rule 4: Outside vet hours (only if vet is scheduled)
			VetSchedule vetSchedule = vetScheduleOpt.get();
			if (startTime.isBefore(vetSchedule.getStartTime()) || endTime.isAfter(vetSchedule.getEndTime())) {
				String vetName = vet != null ? "Dr. " + vet.getLastName() : "Vet";
				conflicts.add(new SchedulingConflict(
						ConflictType.OUTSIDE_VET_HOURS, "Appointment time " + startTime + "-" + endTime + " is outside "
								+ vetName + "'s hours " + vetSchedule.getStartTime() + "-" + vetSchedule.getEndTime(),
						null));
			}
		}

		// Rule 5: Vet time off
		if (vetTimeOffRepo.existsByVetIdAndDate(vetId, date)) {
			String vetName = vet != null ? "Dr. " + vet.getLastName() : "Vet";
			conflicts
				.add(new SchedulingConflict(ConflictType.VET_TIME_OFF, vetName + " has time off on " + date, null));
		}

		// Rule 6: Specialty mismatch
		if (appointmentType != null && appointmentType.getRequiredSpecialty() != null) {
			boolean vetHasSpecialty = vet != null && vet.getSpecialties()
				.stream()
				.anyMatch(s -> s.getId().equals(appointmentType.getRequiredSpecialty().getId()));
			if (!vetHasSpecialty) {
				String vetName = vet != null ? "Dr. " + vet.getLastName() : "Vet";
				String typeName = appointmentType.getName();
				String specialtyName = appointmentType.getRequiredSpecialty().getName();
				conflicts.add(new SchedulingConflict(ConflictType.SPECIALTY_MISMATCH,
						typeName + " requires " + specialtyName + " specialty, but " + vetName + " does not have it",
						null));
			}
		}

		// Rule 7: Vet overlap
		List<Appointment> vetOverlaps = appointmentRepo.findOverlappingByVet(vetId, date, startTime, endTime,
				excludeAppointmentId);
		for (Appointment overlap : vetOverlaps) {
			String vetName = vet != null ? "Dr. " + vet.getLastName() : "Vet";
			String petName = overlap.getPet() != null ? overlap.getPet().getName() : "a pet";
			conflicts.add(new SchedulingConflict(ConflictType.VET_OVERLAP, vetName + " already has an appointment at "
					+ overlap.getStartTime() + "-" + overlap.getEndTime() + " for " + petName, overlap));
		}

		// Rule 8: Pet overlap
		List<Appointment> petOverlaps = appointmentRepo.findOverlappingByPet(petId, date, startTime, endTime,
				excludeAppointmentId);
		for (Appointment overlap : petOverlaps) {
			String petName = overlap.getPet() != null ? overlap.getPet().getName() : "Pet";
			conflicts.add(new SchedulingConflict(ConflictType.PET_OVERLAP,
					petName + " already has an appointment at " + overlap.getStartTime() + "-" + overlap.getEndTime()
							+ " with Dr. " + (overlap.getVet() != null ? overlap.getVet().getLastName() : "unknown"),
					overlap));
		}

		// Rule 9: Owner overlap (only report overlaps from OTHER pets)
		List<Appointment> ownerOverlaps = appointmentRepo.findOverlappingByOwner(ownerId, date, startTime, endTime,
				excludeAppointmentId);
		for (Appointment overlap : ownerOverlaps) {
			// Skip if this is the same pet (already reported as PET_OVERLAP)
			if (overlap.getPet() != null && overlap.getPet().getId() != null
					&& overlap.getPet().getId().equals(petId)) {
				continue;
			}
			String petName = overlap.getPet() != null ? overlap.getPet().getName() : "a pet";
			conflicts.add(new SchedulingConflict(ConflictType.OWNER_OVERLAP, "Owner already has an appointment for "
					+ petName + " at " + overlap.getStartTime() + "-" + overlap.getEndTime(), overlap));
		}

		return new ConflictResult(conflicts);
	}

}
