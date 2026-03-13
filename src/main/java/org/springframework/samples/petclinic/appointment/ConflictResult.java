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
 * Immutable value object containing all scheduling conflicts detected for a proposed
 * appointment.
 */
public record ConflictResult(List<SchedulingConflict> conflicts) {

	/**
	 * Returns {@code true} if at least one conflict was detected.
	 * @return {@code true} if the list is non-empty
	 */
	public boolean hasConflicts() {
		return !conflicts.isEmpty();
	}

	/**
	 * Returns all conflicts of the given type.
	 * @param type the conflict type to filter by
	 * @return list of conflicts matching the given type, possibly empty
	 */
	public List<SchedulingConflict> getConflictsByType(ConflictType type) {
		return conflicts.stream().filter(c -> c.type() == type).toList();
	}

}
