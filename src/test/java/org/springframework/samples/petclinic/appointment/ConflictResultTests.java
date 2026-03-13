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
 * Unit tests for {@link ConflictResult}.
 */
class ConflictResultTests {

	@Test
	void hasConflictsReturnsFalseForEmptyList() {
		ConflictResult result = new ConflictResult(List.of());
		assertThat(result.hasConflicts()).isFalse();
	}

	@Test
	void hasConflictsReturnsTrueForNonEmptyList() {
		SchedulingConflict conflict = new SchedulingConflict(ConflictType.CLINIC_CLOSED, "Clinic is closed", null);
		ConflictResult result = new ConflictResult(List.of(conflict));
		assertThat(result.hasConflicts()).isTrue();
	}

	@Test
	void getConflictsByTypeFiltersCorrectly() {
		SchedulingConflict vetOverlap = new SchedulingConflict(ConflictType.VET_OVERLAP, "vet overlap msg", null);
		SchedulingConflict petOverlap = new SchedulingConflict(ConflictType.PET_OVERLAP, "pet overlap msg", null);
		ConflictResult result = new ConflictResult(List.of(vetOverlap, petOverlap));

		List<SchedulingConflict> vetConflicts = result.getConflictsByType(ConflictType.VET_OVERLAP);
		assertThat(vetConflicts).hasSize(1);
		assertThat(vetConflicts.get(0).type()).isEqualTo(ConflictType.VET_OVERLAP);

		List<SchedulingConflict> petConflicts = result.getConflictsByType(ConflictType.PET_OVERLAP);
		assertThat(petConflicts).hasSize(1);

		List<SchedulingConflict> ownerConflicts = result.getConflictsByType(ConflictType.OWNER_OVERLAP);
		assertThat(ownerConflicts).isEmpty();
	}

}
