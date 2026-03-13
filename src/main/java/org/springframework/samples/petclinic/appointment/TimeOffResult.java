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

import java.util.List;

/**
 * Value object returned by {@link VetScheduleService#addTimeOff}. Contains the saved
 * time-off entry and a warning list of any appointments already scheduled on that date.
 */
public record TimeOffResult(VetTimeOff timeOff, List<Appointment> existingAppointments) {

	/**
	 * Returns {@code true} if there are existing appointments on the time-off date that
	 * staff should handle.
	 * @return {@code true} if the appointment list is non-empty
	 */
	public boolean hasExistingAppointments() {
		return !existingAppointments.isEmpty();
	}

}
