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
import java.util.Map;
import java.util.Optional;

import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing veterinarian weekly schedules and time-off dates.
 *
 * <p>
 * Provides CRUD operations for {@link VetSchedule} records and time-off management via
 * {@link VetTimeOff}. Schedule updates are validated against clinic operating hours from
 * {@link ClinicScheduleConfig}.
 */
@Service
@Transactional
public class VetScheduleService {

	private final VetScheduleRepository vetScheduleRepo;

	private final VetTimeOffRepository vetTimeOffRepo;

	private final ClinicScheduleConfigRepository clinicConfigRepo;

	private final AppointmentRepository appointmentRepo;

	private final VetRepository vetRepo;

	public VetScheduleService(VetScheduleRepository vetScheduleRepo, VetTimeOffRepository vetTimeOffRepo,
			ClinicScheduleConfigRepository clinicConfigRepo, AppointmentRepository appointmentRepo,
			VetRepository vetRepo) {
		this.vetScheduleRepo = vetScheduleRepo;
		this.vetTimeOffRepo = vetTimeOffRepo;
		this.clinicConfigRepo = clinicConfigRepo;
		this.appointmentRepo = appointmentRepo;
		this.vetRepo = vetRepo;
	}

	/**
	 * Returns the vet's full weekly schedule, sorted by day of week.
	 * @param vetId the vet's ID
	 * @return list of {@link VetSchedule} records sorted by dayOfWeek
	 * @throws ResourceNotFoundException if the vet does not exist
	 */
	@Transactional(readOnly = true)
	public List<VetSchedule> getWeeklySchedule(Integer vetId) {
		vetRepo.findById(vetId).orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));
		return vetScheduleRepo.findByVetId(vetId);
	}

	/**
	 * Updates (or creates) a vet's schedule for a specific day of the week.
	 *
	 * <p>
	 * Validates that the proposed times fall within clinic operating hours for that day.
	 * If {@code isAvailable} is {@code true} and the clinic is closed on that day, throws
	 * {@link IllegalArgumentException}.
	 * @param vetId the vet's ID
	 * @param dayOfWeek ISO-8601 day number (1=Monday, 7=Sunday)
	 * @param startTime the vet's start time for this day
	 * @param endTime the vet's end time for this day
	 * @param isAvailable whether the vet works on this day
	 * @return the saved {@link VetSchedule}
	 * @throws ResourceNotFoundException if the vet does not exist
	 * @throws IllegalArgumentException if times are invalid or outside clinic hours
	 */
	public VetSchedule updateDaySchedule(Integer vetId, Integer dayOfWeek, LocalTime startTime, LocalTime endTime,
			boolean isAvailable) {

		var vet = vetRepo.findById(vetId)
			.orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));

		// Validate start < end
		if (!startTime.isBefore(endTime)) {
			throw new IllegalArgumentException("Start time must be before end time");
		}

		// Validate against clinic config
		Optional<ClinicScheduleConfig> clinicConfigOpt = clinicConfigRepo.findByDayOfWeek(dayOfWeek);
		if (clinicConfigOpt.isPresent()) {
			ClinicScheduleConfig config = clinicConfigOpt.get();
			if (isAvailable && !Boolean.TRUE.equals(config.getIsOpen())) {
				throw new IllegalArgumentException("Cannot schedule vet on a day when the clinic is closed");
			}
			if (Boolean.TRUE.equals(config.getIsOpen())) {
				if (startTime.isBefore(config.getOpenTime()) || endTime.isAfter(config.getCloseTime())) {
					throw new IllegalArgumentException("Vet schedule times must fall within clinic hours "
							+ config.getOpenTime() + "-" + config.getCloseTime());
				}
			}
		}

		// Upsert: find existing or create new
		VetSchedule schedule = vetScheduleRepo.findByVetIdAndDayOfWeek(vetId, dayOfWeek).orElseGet(() -> {
			VetSchedule newSchedule = new VetSchedule();
			newSchedule.setVet(vet);
			newSchedule.setDayOfWeek(dayOfWeek);
			return newSchedule;
		});

		schedule.setStartTime(startTime);
		schedule.setEndTime(endTime);
		schedule.setIsAvailable(isAvailable);

		return vetScheduleRepo.save(schedule);
	}

	/**
	 * Updates (or creates) a vet's schedule for all 7 days of the week in a single
	 * transaction.
	 *
	 * <p>
	 * All days are validated first before any are saved. If any day fails validation, no
	 * changes are committed (all-or-nothing). This prevents the partial-save problem
	 * where an error on a later day would leave earlier days already persisted.
	 * @param vetId the vet's ID
	 * @param daySchedules map of ISO day-of-week number (1=Monday..7=Sunday) to
	 * {@link DayScheduleRequest} carrying start time, end time, and availability flag
	 * @return list of the saved {@link VetSchedule} records
	 * @throws ResourceNotFoundException if the vet does not exist
	 * @throws IllegalArgumentException if any day's times are invalid or outside clinic
	 * hours
	 */
	public List<VetSchedule> updateWeekSchedule(Integer vetId, Map<Integer, DayScheduleRequest> daySchedules) {
		// Validate vet exists before iterating (also covers the empty daySchedules case)
		var vet = vetRepo.findById(vetId)
			.orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));

		// Validate all days first before persisting any
		List<VetSchedule> toSave = new ArrayList<>();
		for (Map.Entry<Integer, DayScheduleRequest> entry : daySchedules.entrySet()) {
			Integer dayOfWeek = entry.getKey();
			DayScheduleRequest req = entry.getValue();

			if (!req.startTime().isBefore(req.endTime())) {
				throw new IllegalArgumentException("Day " + dayOfWeek + ": Start time must be before end time");
			}

			Optional<ClinicScheduleConfig> clinicConfigOpt = clinicConfigRepo.findByDayOfWeek(dayOfWeek);
			if (clinicConfigOpt.isPresent()) {
				ClinicScheduleConfig config = clinicConfigOpt.get();
				if (req.isAvailable() && !Boolean.TRUE.equals(config.getIsOpen())) {
					throw new IllegalArgumentException(
							"Day " + dayOfWeek + ": Cannot schedule vet on a day when the clinic is closed");
				}
				if (Boolean.TRUE.equals(config.getIsOpen())) {
					if (req.startTime().isBefore(config.getOpenTime())
							|| req.endTime().isAfter(config.getCloseTime())) {
						throw new IllegalArgumentException(
								"Day " + dayOfWeek + ": Vet schedule times must fall within clinic hours "
										+ config.getOpenTime() + "-" + config.getCloseTime());
					}
				}
			}

			VetSchedule schedule = vetScheduleRepo.findByVetIdAndDayOfWeek(vetId, dayOfWeek).orElseGet(() -> {
				VetSchedule newSchedule = new VetSchedule();
				newSchedule.setVet(vet);
				newSchedule.setDayOfWeek(dayOfWeek);
				return newSchedule;
			});
			schedule.setStartTime(req.startTime());
			schedule.setEndTime(req.endTime());
			schedule.setIsAvailable(req.isAvailable());
			toSave.add(schedule);
		}

		// All days passed validation — now save atomically
		return vetScheduleRepo.saveAll(toSave);
	}

	/**
	 * Carries the start time, end time, and availability flag for a single day in a
	 * week-schedule update request.
	 */
	public record DayScheduleRequest(LocalTime startTime, LocalTime endTime, boolean isAvailable) {

	}

	/**
	 * Returns all time-off entries for a vet, sorted by date.
	 * @param vetId the vet's ID
	 * @return list of {@link VetTimeOff} records
	 */
	@Transactional(readOnly = true)
	public List<VetTimeOff> getTimeOff(Integer vetId) {
		return vetTimeOffRepo.findByVetId(vetId);
	}

	/**
	 * Returns time-off entries for a vet within a date range, sorted by date.
	 * @param vetId the vet's ID
	 * @param start start of the range (inclusive)
	 * @param end end of the range (inclusive)
	 * @return list of {@link VetTimeOff} records within the range
	 */
	@Transactional(readOnly = true)
	public List<VetTimeOff> getTimeOffBetween(Integer vetId, LocalDate start, LocalDate end) {
		return vetTimeOffRepo.findByVetIdAndDateBetween(vetId, start, end);
	}

	/**
	 * Adds a time-off date for a vet.
	 *
	 * <p>
	 * Returns a {@link TimeOffResult} containing the saved entity and a list of existing
	 * appointments on that date as warnings. Does NOT cancel any appointments
	 * automatically -- staff must handle them manually.
	 * @param vetId the vet's ID
	 * @param date the date the vet will be away
	 * @param reason optional reason description
	 * @return a {@link TimeOffResult} with the saved time-off and any existing
	 * appointments
	 * @throws ResourceNotFoundException if the vet does not exist
	 * @throws IllegalArgumentException if the date is in the past
	 */
	public TimeOffResult addTimeOff(Integer vetId, LocalDate date, String reason) {
		var vet = vetRepo.findById(vetId)
			.orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));

		if (date.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Cannot add time off for past dates");
		}

		// Check for existing appointments (warning only -- not prevented)
		List<Appointment> existing = appointmentRepo.findByVetIdAndDate(vetId, date);

		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setVet(vet);
		timeOff.setDate(date);
		timeOff.setReason(reason);

		VetTimeOff saved = vetTimeOffRepo.save(timeOff);
		return new TimeOffResult(saved, existing);
	}

	/**
	 * Removes a time-off entry by ID, verifying it belongs to the specified vet.
	 * @param vetId the vet's ID (used to verify ownership)
	 * @param timeOffId the time-off entry ID
	 * @throws ResourceNotFoundException if no entry with this ID exists or it does not
	 * belong to the given vet
	 */
	public void removeTimeOff(Integer vetId, Integer timeOffId) {
		VetTimeOff timeOff = vetTimeOffRepo.findById(timeOffId)
			.orElseThrow(() -> new ResourceNotFoundException("VetTimeOff not found with id: " + timeOffId));
		if (!vetId.equals(timeOff.getVet().getId())) {
			throw new ResourceNotFoundException("VetTimeOff " + timeOffId + " does not belong to vet " + vetId);
		}
		vetTimeOffRepo.delete(timeOff);
	}

}
