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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for creating, updating, and managing the lifecycle of {@link Appointment}
 * entities.
 *
 * <p>
 * All mutating methods are transactional. Conflict detection is delegated to
 * {@link ConflictDetectionService}. On completion, a {@link Visit} record is
 * automatically created for the pet.
 */
@Service
@Transactional
public class AppointmentService {

	private final AppointmentRepository appointmentRepo;

	private final AppointmentTypeRepository appointmentTypeRepo;

	private final ConflictDetectionService conflictDetectionService;

	private final OwnerRepository ownerRepo;

	private final VetRepository vetRepo;

	public AppointmentService(AppointmentRepository appointmentRepo, AppointmentTypeRepository appointmentTypeRepo,
			ConflictDetectionService conflictDetectionService, OwnerRepository ownerRepo, VetRepository vetRepo) {
		this.appointmentRepo = appointmentRepo;
		this.appointmentTypeRepo = appointmentTypeRepo;
		this.conflictDetectionService = conflictDetectionService;
		this.ownerRepo = ownerRepo;
		this.vetRepo = vetRepo;
	}

	/**
	 * Creates a new appointment after validating all conflict rules.
	 * @throws ResourceNotFoundException if vet, appointmentType, owner, or pet not found
	 * @throws SchedulingConflictException if any conflicts are detected
	 */
	public Appointment createAppointment(Integer petId, Integer ownerId, Integer vetId, Integer appointmentTypeId,
			LocalDate date, LocalTime startTime, String notes) {

		Vet vet = vetRepo.findById(vetId)
			.orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));

		AppointmentType appointmentType = appointmentTypeRepo.findByIdWithSpecialty(appointmentTypeId)
			.orElseThrow(
					() -> new ResourceNotFoundException("AppointmentType not found with id: " + appointmentTypeId));

		Owner owner = ownerRepo.findById(ownerId)
			.orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new ResourceNotFoundException("Pet not found with id: " + petId + " for owner with id: " + ownerId);
		}

		LocalTime endTime = startTime.plusMinutes(appointmentType.getDefaultDurationMinutes());

		ConflictResult conflicts = conflictDetectionService.detectConflicts(vetId, petId, ownerId, date, startTime,
				endTime, appointmentType, vet, null);

		if (conflicts.hasConflicts()) {
			throw new SchedulingConflictException(conflicts);
		}

		Appointment appointment = new Appointment();
		appointment.setAppointmentDate(date);
		appointment.setStartTime(startTime);
		appointment.setEndTime(endTime);
		appointment.setNotes(notes);
		appointment.setVet(vet);
		appointment.setPet(pet);
		appointment.setAppointmentType(appointmentType);

		return appointmentRepo.save(appointment);
	}

	/**
	 * Updates an existing appointment's date, time, or vet.
	 *
	 * <p>
	 * Only SCHEDULED and CONFIRMED appointments can be edited. Re-validates all conflict
	 * rules after changes, excluding the appointment itself from overlap checks.
	 * @throws ResourceNotFoundException if appointment not found
	 * @throws IllegalStateException if appointment is CANCELLED or COMPLETED
	 * @throws SchedulingConflictException if any conflicts are detected after changes
	 */
	public Appointment updateAppointment(Integer appointmentId, Integer vetId, LocalDate date, LocalTime startTime,
			String notes) {

		Appointment appointment = appointmentRepo.findByIdWithDetails(appointmentId)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

		AppointmentStatus currentStatus = appointment.getStatus();
		if (currentStatus == AppointmentStatus.CANCELLED || currentStatus == AppointmentStatus.COMPLETED) {
			throw new IllegalStateException("Cannot update appointment in " + currentStatus.name()
					+ " status. Only SCHEDULED and CONFIRMED appointments can be edited.");
		}

		Vet vet = appointment.getVet();
		if (!vet.getId().equals(vetId)) {
			vet = vetRepo.findById(vetId)
				.orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));
		}

		AppointmentType appointmentType = appointment.getAppointmentType();
		LocalTime endTime = startTime.plusMinutes(appointmentType.getDefaultDurationMinutes());

		Integer petId = appointment.getPet().getId();
		Integer ownerId = appointment.getPet().getOwner().getId();

		ConflictResult conflicts = conflictDetectionService.detectConflicts(vetId, petId, ownerId, date, startTime,
				endTime, appointmentType, vet, appointmentId);

		if (conflicts.hasConflicts()) {
			throw new SchedulingConflictException(conflicts);
		}

		appointment.setVet(vet);
		appointment.setAppointmentDate(date);
		appointment.setStartTime(startTime);
		appointment.setEndTime(endTime);
		appointment.setNotes(notes);

		return appointmentRepo.save(appointment);
	}

	/**
	 * Transitions appointment to CONFIRMED.
	 * @throws ResourceNotFoundException if appointment not found
	 * @throws IllegalStateException if current status cannot transition to CONFIRMED
	 */
	public Appointment confirmAppointment(Integer appointmentId) {
		Appointment appointment = appointmentRepo.findByIdWithDetails(appointmentId)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
		appointment.setStatus(AppointmentStatus.CONFIRMED);
		return appointmentRepo.save(appointment);
	}

	/**
	 * Transitions appointment to CANCELLED, sets cancelledAt timestamp.
	 * @throws ResourceNotFoundException if appointment not found
	 * @throws IllegalStateException if current status cannot transition to CANCELLED
	 */
	public Appointment cancelAppointment(Integer appointmentId) {
		Appointment appointment = appointmentRepo.findByIdWithDetails(appointmentId)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
		appointment.setStatus(AppointmentStatus.CANCELLED);
		appointment.setCancelledAt(LocalDateTime.now());
		return appointmentRepo.save(appointment);
	}

	/**
	 * Transitions appointment to COMPLETED, creates a Visit record for the pet.
	 * @throws ResourceNotFoundException if appointment not found
	 * @throws IllegalStateException if current status cannot transition to COMPLETED
	 */
	public Appointment completeAppointment(Integer appointmentId) {
		Appointment appointment = appointmentRepo.findByIdWithDetails(appointmentId)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
		appointment.setStatus(AppointmentStatus.COMPLETED);

		Integer ownerId = appointment.getPet().getOwner().getId();
		Owner owner = ownerRepo.findById(ownerId)
			.orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

		Visit visit = new Visit();
		visit.setDate(appointment.getAppointmentDate());
		visit.setDescription(buildVisitDescription(appointment));
		owner.addVisit(appointment.getPet().getId(), visit);
		ownerRepo.save(owner);

		return appointmentRepo.save(appointment);
	}

	/**
	 * Finds appointment by ID with all associations loaded.
	 */
	@Transactional(readOnly = true)
	public Optional<Appointment> findById(Integer id) {
		return appointmentRepo.findByIdWithDetails(id);
	}

	/**
	 * Finds appointments for a specific date (all vets).
	 */
	@Transactional(readOnly = true)
	public List<Appointment> findByDate(LocalDate date) {
		return appointmentRepo.findByDate(date);
	}

	/**
	 * Finds appointments for a vet within a date range.
	 */
	@Transactional(readOnly = true)
	public List<Appointment> findByVetAndDateRange(Integer vetId, LocalDate start, LocalDate end) {
		return appointmentRepo.findByVetIdAndDateBetween(vetId, start, end);
	}

	private String buildVisitDescription(Appointment appointment) {
		StringBuilder sb = new StringBuilder();
		sb.append(appointment.getAppointmentType().getName());
		sb.append(" with Dr. ").append(appointment.getVet().getLastName());
		if (appointment.getNotes() != null && !appointment.getNotes().isBlank()) {
			sb.append(" - ").append(appointment.getNotes());
		}
		return sb.toString();
	}

}
