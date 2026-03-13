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

import java.time.LocalTime;

/**
 * Immutable value object representing a bookable time slot.
 *
 * <p>
 * A {@code TimeSlot} has a start and end time and can test whether it overlaps with
 * another time range.
 */
public record TimeSlot(LocalTime startTime, LocalTime endTime) {

	/**
	 * Returns {@code true} if this slot's time range overlaps with the given range.
	 *
	 * <p>
	 * Uses the standard half-open interval overlap formula: {@code this.start < otherEnd
	 * AND this.end > otherStart}.
	 * @param otherStart the start of the other range (inclusive)
	 * @param otherEnd the end of the other range (exclusive)
	 * @return {@code true} if the ranges overlap
	 */
	public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
		return this.startTime.isBefore(otherEnd) && this.endTime.isAfter(otherStart);
	}

}
