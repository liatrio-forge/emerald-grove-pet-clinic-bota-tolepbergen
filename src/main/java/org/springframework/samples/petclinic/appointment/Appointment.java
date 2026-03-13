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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a scheduled appointment between a pet, a vet, and an appointment type.
 *
 * <p>
 * All FK relationships are {@link FetchType#LAZY} -- critical because existing
 * {@code Pet.visits} is {@code EAGER} and adding eager loading here would cascade. All
 * repository queries use JOIN FETCH to avoid LazyInitializationException.
 *
 * <p>
 * {@code createdAt} is set automatically via {@link PrePersist} and is not updatable.
 * {@code status} defaults to {@link AppointmentStatus#SCHEDULED}.
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

	@Column(name = "appointment_date")
	@NotNull
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate appointmentDate;

	@Column(name = "start_time")
	@NotNull
	private LocalTime startTime;

	@Column(name = "end_time")
	@NotNull
	private LocalTime endTime;

	@Column(name = "status")
	@NotNull
	@Enumerated(EnumType.STRING)
	private AppointmentStatus status = AppointmentStatus.SCHEDULED;

	@Column(name = "notes")
	@Size(max = 500)
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id", nullable = false)
	@NotNull
	private Pet pet;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vet_id", nullable = false)
	@NotNull
	private Vet vet;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_type_id", nullable = false)
	@NotNull
	private AppointmentType appointmentType;

	@Column(name = "created_at", updatable = false)
	@NotNull
	private LocalDateTime createdAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Version
	private Integer version;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public LocalDate getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDate appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	@AssertTrue(message = "startTime must be before endTime")
	private boolean isStartBeforeEnd() {
		return startTime == null || endTime == null || startTime.isBefore(endTime);
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	/**
	 * Sets the appointment status, enforcing valid state-machine transitions.
	 *
	 * <p>
	 * When the current status is already set, the transition is validated via
	 * {@link AppointmentStatus#transitionTo(AppointmentStatus)}, which throws
	 * {@link IllegalStateException} for disallowed transitions.
	 */
	public void setStatus(AppointmentStatus status) {
		if (this.status != null) {
			this.status = this.status.transitionTo(status);
		}
		else {
			this.status = status;
		}
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public Vet getVet() {
		return vet;
	}

	public void setVet(Vet vet) {
		this.vet = vet;
	}

	public AppointmentType getAppointmentType() {
		return appointmentType;
	}

	public void setAppointmentType(AppointmentType appointmentType) {
		this.appointmentType = appointmentType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public Integer getVersion() {
		return version;
	}

	void setVersion(Integer version) {
		this.version = version;
	}

}
