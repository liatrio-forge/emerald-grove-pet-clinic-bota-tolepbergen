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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the clinic's per-day operating hours and slot configuration.
 *
 * <p>
 * One row per day of the week (7 rows total). The {@code dayOfWeek} field uses ISO-8601
 * numbering (1=Monday, 7=Sunday), matching {@link DayOfWeek#getValue()}.
 */
@Entity
@Table(name = "clinic_schedule_config")
public class ClinicScheduleConfig extends BaseEntity {

	@Column(name = "day_of_week")
	@NotNull
	@Min(1)
	@Max(7)
	private Integer dayOfWeek;

	@Column(name = "open_time")
	@NotNull
	private LocalTime openTime;

	@Column(name = "close_time")
	@NotNull
	private LocalTime closeTime;

	@Column(name = "slot_duration_minutes")
	@NotNull
	@Min(5)
	@Max(1440)
	private Integer slotDurationMinutes;

	@Column(name = "is_open")
	@NotNull
	private Boolean isOpen;

	@Version
	private Integer version;

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	@AssertTrue(message = "openTime must be before closeTime when the day is open")
	private boolean isValidTimeWindow() {
		if (!Boolean.TRUE.equals(isOpen)) {
			return true;
		}
		return openTime != null && closeTime != null && openTime.isBefore(closeTime);
	}

	/**
	 * Returns the {@link DayOfWeek} enum corresponding to the stored integer value.
	 * @return the {@link DayOfWeek} for this configuration
	 */
	public DayOfWeek getDayOfWeekEnum() {
		return DayOfWeek.of(this.dayOfWeek);
	}

	public LocalTime getOpenTime() {
		return openTime;
	}

	public void setOpenTime(LocalTime openTime) {
		this.openTime = openTime;
	}

	public LocalTime getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(LocalTime closeTime) {
		this.closeTime = closeTime;
	}

	public Integer getSlotDurationMinutes() {
		return slotDurationMinutes;
	}

	public void setSlotDurationMinutes(Integer slotDurationMinutes) {
		this.slotDurationMinutes = slotDurationMinutes;
	}

	public Boolean getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(Boolean isOpen) {
		this.isOpen = isOpen;
	}

	public Integer getVersion() {
		return version;
	}

	void setVersion(Integer version) {
		this.version = version;
	}

}
