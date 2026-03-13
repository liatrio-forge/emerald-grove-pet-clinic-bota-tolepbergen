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

import org.springframework.samples.petclinic.model.NamedEntity;
import org.springframework.samples.petclinic.vet.Specialty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a category of appointment offered by the clinic (e.g. Checkup, Surgery).
 *
 * <p>
 * Extends {@link NamedEntity} to inherit the {@code name} field with {@code @NotBlank}
 * validation. The {@code requiredSpecialty} is optional -- general-purpose types like
 * "Checkup" have no specialty requirement.
 */
@Entity
@Table(name = "appointment_types")
public class AppointmentType extends NamedEntity {

	@Column(name = "default_duration_minutes")
	@NotNull
	@Min(1)
	private Integer defaultDurationMinutes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specialty_id")
	private Specialty requiredSpecialty;

	@Column(name = "description")
	@Size(max = 255)
	private String description;

	@Version
	private Integer version;

	public Integer getDefaultDurationMinutes() {
		return defaultDurationMinutes;
	}

	public void setDefaultDurationMinutes(Integer defaultDurationMinutes) {
		this.defaultDurationMinutes = defaultDurationMinutes;
	}

	public Specialty getRequiredSpecialty() {
		return requiredSpecialty;
	}

	public void setRequiredSpecialty(Specialty requiredSpecialty) {
		this.requiredSpecialty = requiredSpecialty;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
