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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the lifecycle status of an {@link Appointment}.
 *
 * <p>
 * State transition rules:
 * <ul>
 * <li>SCHEDULED -&gt; CONFIRMED, CANCELLED, COMPLETED</li>
 * <li>CONFIRMED -&gt; CANCELLED, COMPLETED</li>
 * <li>CANCELLED -&gt; (terminal, no transitions)</li>
 * <li>COMPLETED -&gt; (terminal, no transitions)</li>
 * </ul>
 */
public enum AppointmentStatus {

	SCHEDULED, CONFIRMED, CANCELLED, COMPLETED;

	private Set<AppointmentStatus> allowedTransitions;

	static {
		SCHEDULED.allowedTransitions = Collections.unmodifiableSet(EnumSet.of(CONFIRMED, CANCELLED, COMPLETED));
		CONFIRMED.allowedTransitions = Collections.unmodifiableSet(EnumSet.of(CANCELLED, COMPLETED));
		CANCELLED.allowedTransitions = Collections.unmodifiableSet(EnumSet.noneOf(AppointmentStatus.class));
		COMPLETED.allowedTransitions = Collections.unmodifiableSet(EnumSet.noneOf(AppointmentStatus.class));
	}

	/**
	 * Returns {@code true} if this status can transition to the given target status.
	 * @param target the desired target status
	 * @return {@code true} if the transition is allowed
	 */
	public boolean canTransitionTo(AppointmentStatus target) {
		return this.allowedTransitions.contains(target);
	}

	/**
	 * Validates and returns the target status after a state transition.
	 * @param target the desired target status
	 * @return the target status if the transition is allowed
	 * @throws IllegalStateException if the transition is not allowed
	 */
	public AppointmentStatus transitionTo(AppointmentStatus target) {
		if (target == null) {
			throw new IllegalArgumentException("Target status must not be null");
		}
		if (!canTransitionTo(target)) {
			throw new IllegalStateException(
					"Cannot transition from " + this.name() + " to " + target.name() + ". Invalid state transition.");
		}
		return target;
	}

}
