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
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link Appointment} entities.
 *
 * <p>
 * All queries use JOIN FETCH for pet, vet, and appointmentType because
 * {@code spring.jpa.open-in-view=false}.
 *
 * <p>
 * Overlap detection uses the standard interval formula:
 * {@code existing.startTime < newEnd AND existing.endTime > newStart}
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

	/**
	 * Find overlapping non-cancelled appointments for a specific vet on a date. An
	 * optional {@code excludeId} allows self-exclusion during updates.
	 */
	@Transactional(readOnly = true)
	@Query("""
			SELECT a FROM Appointment a
			JOIN FETCH a.pet p
			JOIN FETCH a.vet v
			JOIN FETCH a.appointmentType at
			WHERE a.vet.id = :vetId
			  AND a.appointmentDate = :date
			  AND a.status <> 'CANCELLED'
			  AND a.startTime < :endTime
			  AND a.endTime > :startTime
			  AND (:excludeId IS NULL OR a.id <> :excludeId)
			""")
	List<Appointment> findOverlappingByVet(@Param("vetId") Integer vetId, @Param("date") LocalDate date,
			@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
			@Param("excludeId") Integer excludeId);

	/**
	 * Find overlapping non-cancelled appointments for a specific pet on a date.
	 */
	@Transactional(readOnly = true)
	@Query("""
			SELECT a FROM Appointment a
			JOIN FETCH a.pet p
			JOIN FETCH a.vet v
			JOIN FETCH a.appointmentType at
			WHERE a.pet.id = :petId
			  AND a.appointmentDate = :date
			  AND a.status <> 'CANCELLED'
			  AND a.startTime < :endTime
			  AND a.endTime > :startTime
			  AND (:excludeId IS NULL OR a.id <> :excludeId)
			""")
	List<Appointment> findOverlappingByPet(@Param("petId") Integer petId, @Param("date") LocalDate date,
			@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
			@Param("excludeId") Integer excludeId);

	/**
	 * Find overlapping non-cancelled appointments for any pet belonging to a specific
	 * owner.
	 */
	@Transactional(readOnly = true)
	@Query("""
			SELECT a FROM Appointment a
			JOIN FETCH a.pet p
			JOIN FETCH a.vet v
			JOIN FETCH a.appointmentType at
			WHERE p.owner.id = :ownerId
			  AND a.appointmentDate = :date
			  AND a.status <> 'CANCELLED'
			  AND a.startTime < :endTime
			  AND a.endTime > :startTime
			  AND (:excludeId IS NULL OR a.id <> :excludeId)
			""")
	List<Appointment> findOverlappingByOwner(@Param("ownerId") Integer ownerId, @Param("date") LocalDate date,
			@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
			@Param("excludeId") Integer excludeId);

	/**
	 * Find all non-cancelled appointments for a vet on a specific date, sorted by start
	 * time. Used by AvailabilityService to compute occupied slots.
	 */
	@Transactional(readOnly = true)
	@Query("""
			SELECT a FROM Appointment a
			JOIN FETCH a.pet p
			JOIN FETCH a.vet v
			JOIN FETCH a.appointmentType at
			WHERE a.vet.id = :vetId
			  AND a.appointmentDate = :date
			  AND a.status <> 'CANCELLED'
			ORDER BY a.startTime ASC
			""")
	List<Appointment> findByVetIdAndDate(@Param("vetId") Integer vetId, @Param("date") LocalDate date);

	/**
	 * Find all non-cancelled appointments for a vet within a date range. Used by weekly
	 * calendar view.
	 */
	@Transactional(readOnly = true)
	@Query("""
			SELECT a FROM Appointment a
			JOIN FETCH a.pet p
			JOIN FETCH a.vet v
			JOIN FETCH a.appointmentType at
			WHERE a.vet.id = :vetId
			  AND a.appointmentDate BETWEEN :startDate AND :endDate
			  AND a.status <> 'CANCELLED'
			ORDER BY a.appointmentDate ASC, a.startTime ASC
			""")
	List<Appointment> findByVetIdAndDateBetween(@Param("vetId") Integer vetId, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	/**
	 * Find all non-cancelled appointments for a specific date (all vets). Used by daily
	 * calendar view.
	 */
	@Transactional(readOnly = true)
	@Query("""
			SELECT a FROM Appointment a
			JOIN FETCH a.pet p
			JOIN FETCH a.vet v
			JOIN FETCH a.appointmentType at
			WHERE a.appointmentDate = :date
			  AND a.status <> 'CANCELLED'
			ORDER BY a.vet.id ASC, a.startTime ASC
			""")
	List<Appointment> findByDate(@Param("date") LocalDate date);

	/**
	 * Find a single appointment with all associations eagerly loaded, including the
	 * required specialty of the appointment type.
	 */
	@Transactional(readOnly = true)
	@Query("""
			SELECT a FROM Appointment a
			JOIN FETCH a.pet p
			JOIN FETCH a.vet v
			JOIN FETCH a.appointmentType at
			LEFT JOIN FETCH at.requiredSpecialty
			WHERE a.id = :id
			""")
	Optional<Appointment> findByIdWithDetails(@Param("id") Integer id);

}
