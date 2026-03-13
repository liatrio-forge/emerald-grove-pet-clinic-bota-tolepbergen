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
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link VetSchedule} entities.
 *
 * <p>
 * All queries use JOIN FETCH for the vet association because
 * {@code spring.jpa.open-in-view=false}.
 */
public interface VetScheduleRepository extends JpaRepository<VetSchedule, Integer> {

	@Transactional(readOnly = true)
	@Query("SELECT vs FROM VetSchedule vs JOIN FETCH vs.vet WHERE vs.vet.id = :vetId ORDER BY vs.dayOfWeek ASC")
	List<VetSchedule> findByVetId(@Param("vetId") Integer vetId);

	@Transactional(readOnly = true)
	@Query("SELECT vs FROM VetSchedule vs JOIN FETCH vs.vet WHERE vs.vet.id = :vetId AND vs.dayOfWeek = :dayOfWeek")
	Optional<VetSchedule> findByVetIdAndDayOfWeek(@Param("vetId") Integer vetId, @Param("dayOfWeek") Integer dayOfWeek);

	@Transactional(readOnly = true)
	@Query("SELECT vs FROM VetSchedule vs JOIN FETCH vs.vet WHERE vs.dayOfWeek = :dayOfWeek AND vs.isAvailable = true")
	List<VetSchedule> findAvailableByDayOfWeek(@Param("dayOfWeek") Integer dayOfWeek);

	@Transactional(readOnly = true)
	@Query("""
			SELECT vs FROM VetSchedule vs JOIN FETCH vs.vet
			WHERE vs.vet.id = :vetId AND vs.isAvailable = true
			ORDER BY vs.dayOfWeek ASC
			""")
	List<VetSchedule> findAvailableByVetId(@Param("vetId") Integer vetId);

}
