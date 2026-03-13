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
 * Unit tests for {@link SchedulingConflictException}.
 */
class SchedulingConflictExceptionTests {

	@Test
	void exceptionHoldsConflictResult() {
		// Arrange
		SchedulingConflict conflict = new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet overlap", null);
		ConflictResult conflictResult = new ConflictResult(List.of(conflict));

		// Act
		SchedulingConflictException ex = new SchedulingConflictException(conflictResult);

		// Assert
		assertThat(ex.getConflictResult()).isSameAs(conflictResult);
	}

	@Test
	void exceptionMessageIncludesConflictCount() {
		// Arrange
		SchedulingConflict conflict1 = new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet overlap", null);
		SchedulingConflict conflict2 = new SchedulingConflict(ConflictType.PET_OVERLAP, "Pet overlap", null);
		ConflictResult conflictResult = new ConflictResult(List.of(conflict1, conflict2));

		// Act
		SchedulingConflictException ex = new SchedulingConflictException(conflictResult);

		// Assert
		assertThat(ex.getMessage()).contains("2").contains("conflict");
	}

}
