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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TimeOffResult}.
 */
class TimeOffResultTests {

	@Test
	void hasExistingAppointmentsReturnsFalseForEmptyList() {
		VetTimeOff timeOff = new VetTimeOff();
		TimeOffResult result = new TimeOffResult(timeOff, List.of());
		assertThat(result.hasExistingAppointments()).isFalse();
	}

	@Test
	void hasExistingAppointmentsReturnsTrueForNonEmptyList() {
		VetTimeOff timeOff = new VetTimeOff();
		Appointment appt = new Appointment();
		TimeOffResult result = new TimeOffResult(timeOff, List.of(appt));
		assertThat(result.hasExistingAppointments()).isTrue();
	}

}
