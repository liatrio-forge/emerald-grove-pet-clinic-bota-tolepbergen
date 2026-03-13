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
import java.time.LocalTime;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.vet.Vet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a veterinarian's weekly working schedule for a specific day.
 *
 * <p>
 * The {@code vet} association is loaded lazily. All repository queries use JOIN FETCH to
 * avoid {@code LazyInitializationException} when {@code spring.jpa.open-in-view=false}.
 */
@Entity
@Table(name = "vet_schedules", uniqueConstraints = { @UniqueConstraint(columnNames = { "vet_id", "day_of_week" }) })
public class VetSchedule extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vet_id", nullable = false)
	@NotNull
	private Vet vet;

	@Column(name = "day_of_week")
	@NotNull
	@Min(1)
	@Max(7)
	private Integer dayOfWeek;

	@Column(name = "start_time")
	@NotNull
	private LocalTime startTime;

	@Column(name = "end_time")
	@NotNull
	private LocalTime endTime;

	@Column(name = "is_available")
	@NotNull
	private Boolean isAvailable;

	@Version
	private Integer version;

	public Vet getVet() {
		return vet;
	}

	public void setVet(Vet vet) {
		this.vet = vet;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * Returns the {@link DayOfWeek} enum corresponding to the stored integer value.
	 * @return the {@link DayOfWeek} for this schedule
	 */
	public DayOfWeek getDayOfWeekEnum() {
		return DayOfWeek.of(this.dayOfWeek);
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

	@AssertTrue(message = "Start time must be before end time")
	private boolean isStartBeforeEnd() {
		return startTime == null || endTime == null || startTime.isBefore(endTime);
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public Integer getVersion() {
		return version;
	}

	void setVersion(Integer version) {
		this.version = version;
	}

}
