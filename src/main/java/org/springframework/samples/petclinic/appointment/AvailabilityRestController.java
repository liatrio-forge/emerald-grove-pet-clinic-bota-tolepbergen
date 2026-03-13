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
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing availability endpoints for the AJAX-driven appointment booking
 * form.
 *
 * <p>
 * Endpoints:
 * <ul>
 * <li>{@code GET /api/vets/available} — returns vets available on a date, optionally
 * filtered by specialty</li>
 * <li>{@code GET /api/slots/available} — returns available time slots for a vet on a
 * date</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
class AvailabilityRestController {

	private final AvailabilityService availabilityService;

	AvailabilityRestController(AvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	/**
	 * Returns vets available on the given date, optionally filtered by specialty.
	 * @param date the desired date (ISO format yyyy-MM-dd, required)
	 * @param specialtyId optional specialty ID filter; if absent, all available vets are
	 * returned
	 * @return JSON array of {@link VetAvailabilityDto}
	 */
	@GetMapping("/vets/available")
	public List<VetAvailabilityDto> getAvailableVets(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Integer specialtyId) {
		return availabilityService.getAvailableVets(date, specialtyId)
			.stream()
			.map(VetAvailabilityDto::fromVet)
			.toList();
	}

	/**
	 * Returns available time slots for a vet on the given date.
	 * @param vetId the vet's ID (required)
	 * @param date the desired date (ISO format yyyy-MM-dd, required)
	 * @param durationMinutes the appointment duration in minutes (default 30)
	 * @return JSON array of {@link TimeSlotDto}
	 */
	@GetMapping("/slots/available")
	public List<TimeSlotDto> getAvailableSlots(@RequestParam Integer vetId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(defaultValue = "30") int durationMinutes) {
		return availabilityService.getAvailableSlots(vetId, date, durationMinutes)
			.stream()
			.map(TimeSlotDto::fromTimeSlot)
			.toList();
	}

}
