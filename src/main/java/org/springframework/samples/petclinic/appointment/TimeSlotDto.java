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

/**
 * Data Transfer Object representing an available time slot for the REST API.
 *
 * <p>
 * Returned by {@link AvailabilityRestController#getAvailableSlots}.
 *
 * @param startTime the start time as a string in "HH:mm" format (e.g., "09:00")
 * @param endTime the end time as a string in "HH:mm" format (e.g., "09:30")
 */
public record TimeSlotDto(String startTime, String endTime) {

	private static final java.time.format.DateTimeFormatter TIME_FORMAT = java.time.format.DateTimeFormatter
		.ofPattern("HH:mm");

	/**
	 * Creates a {@code TimeSlotDto} from a {@link TimeSlot} value object.
	 * @param slot the time slot
	 * @return a new DTO with times formatted as "HH:mm"
	 */
	public static TimeSlotDto fromTimeSlot(TimeSlot slot) {
		return new TimeSlotDto(slot.startTime().format(TIME_FORMAT), slot.endTime().format(TIME_FORMAT));
	}

}
