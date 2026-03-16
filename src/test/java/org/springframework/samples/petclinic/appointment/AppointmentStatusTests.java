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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AppointmentStatus} state transition logic.
 */
class AppointmentStatusTests {

	// SCHEDULED -> CONFIRMED
	@Test
	void scheduledCanTransitionToConfirmed() {
		assertThat(AppointmentStatus.SCHEDULED.canTransitionTo(AppointmentStatus.CONFIRMED)).isTrue();
	}

	// SCHEDULED -> CANCELLED
	@Test
	void scheduledCanTransitionToCancelled() {
		assertThat(AppointmentStatus.SCHEDULED.canTransitionTo(AppointmentStatus.CANCELLED)).isTrue();
	}

	// SCHEDULED -> COMPLETED
	@Test
	void scheduledCanTransitionToCompleted() {
		assertThat(AppointmentStatus.SCHEDULED.canTransitionTo(AppointmentStatus.COMPLETED)).isTrue();
	}

	// CONFIRMED -> CANCELLED
	@Test
	void confirmedCanTransitionToCancelled() {
		assertThat(AppointmentStatus.CONFIRMED.canTransitionTo(AppointmentStatus.CANCELLED)).isTrue();
	}

	// CONFIRMED -> COMPLETED
	@Test
	void confirmedCanTransitionToCompleted() {
		assertThat(AppointmentStatus.CONFIRMED.canTransitionTo(AppointmentStatus.COMPLETED)).isTrue();
	}

	// CONFIRMED -> SCHEDULED (backward) - not allowed
	@Test
	void confirmedCannotTransitionToScheduled() {
		assertThat(AppointmentStatus.CONFIRMED.canTransitionTo(AppointmentStatus.SCHEDULED)).isFalse();
	}

	// CONFIRMED -> CONFIRMED (self) - not allowed
	@Test
	void confirmedCannotTransitionToItself() {
		assertThat(AppointmentStatus.CONFIRMED.canTransitionTo(AppointmentStatus.CONFIRMED)).isFalse();
	}

	// CANCELLED is terminal
	@Test
	void cancelledCannotTransitionToScheduled() {
		assertThat(AppointmentStatus.CANCELLED.canTransitionTo(AppointmentStatus.SCHEDULED)).isFalse();
	}

	@Test
	void cancelledCannotTransitionToConfirmed() {
		assertThat(AppointmentStatus.CANCELLED.canTransitionTo(AppointmentStatus.CONFIRMED)).isFalse();
	}

	@Test
	void cancelledCannotTransitionToCompleted() {
		assertThat(AppointmentStatus.CANCELLED.canTransitionTo(AppointmentStatus.COMPLETED)).isFalse();
	}

	@Test
	void cancelledCannotTransitionToItself() {
		assertThat(AppointmentStatus.CANCELLED.canTransitionTo(AppointmentStatus.CANCELLED)).isFalse();
	}

	// COMPLETED is terminal
	@Test
	void completedCannotTransitionToScheduled() {
		assertThat(AppointmentStatus.COMPLETED.canTransitionTo(AppointmentStatus.SCHEDULED)).isFalse();
	}

	@Test
	void completedCannotTransitionToConfirmed() {
		assertThat(AppointmentStatus.COMPLETED.canTransitionTo(AppointmentStatus.CONFIRMED)).isFalse();
	}

	@Test
	void completedCannotTransitionToCancelled() {
		assertThat(AppointmentStatus.COMPLETED.canTransitionTo(AppointmentStatus.CANCELLED)).isFalse();
	}

	@Test
	void completedCannotTransitionToItself() {
		assertThat(AppointmentStatus.COMPLETED.canTransitionTo(AppointmentStatus.COMPLETED)).isFalse();
	}

	// SCHEDULED cannot self-transition
	@Test
	void scheduledCannotTransitionToItself() {
		assertThat(AppointmentStatus.SCHEDULED.canTransitionTo(AppointmentStatus.SCHEDULED)).isFalse();
	}

	// transitionTo succeeds for valid transition
	@Test
	void transitionToReturnsTargetForValidTransition() {
		AppointmentStatus result = AppointmentStatus.SCHEDULED.transitionTo(AppointmentStatus.CONFIRMED);
		assertThat(result).isEqualTo(AppointmentStatus.CONFIRMED);
	}

	// transitionTo throws IllegalStateException for invalid transition
	@Test
	void transitionToThrowsIllegalStateExceptionForInvalidTransition() {
		assertThatThrownBy(() -> AppointmentStatus.CANCELLED.transitionTo(AppointmentStatus.CONFIRMED))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("CANCELLED")
			.hasMessageContaining("CONFIRMED");
	}

	@Test
	void transitionToThrowsIllegalStateExceptionForCompletedTerminalStatus() {
		assertThatThrownBy(() -> AppointmentStatus.COMPLETED.transitionTo(AppointmentStatus.SCHEDULED))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void canTransitionToReturnsFalseForNullTarget() {
		assertThat(AppointmentStatus.SCHEDULED.canTransitionTo(null)).isFalse();
	}

	@Test
	void transitionToThrowsIllegalArgumentExceptionForNullTarget() {
		assertThatThrownBy(() -> AppointmentStatus.SCHEDULED.transitionTo(null))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
