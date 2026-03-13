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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link VetTimeOff} entities.
 *
 * <p>
 * All queries returning entities use JOIN FETCH for the vet association because
 * {@code spring.jpa.open-in-view=false}.
 */
public interface VetTimeOffRepository extends JpaRepository<VetTimeOff, Integer> {

	@Transactional(readOnly = true)
	@Query("SELECT vto FROM VetTimeOff vto JOIN FETCH vto.vet WHERE vto.vet.id = :vetId AND vto.date = :date")
	Optional<VetTimeOff> findByVetIdAndDate(@Param("vetId") Integer vetId, @Param("date") LocalDate date);

	@Transactional(readOnly = true)
	boolean existsByVetIdAndDate(Integer vetId, LocalDate date);

	@Transactional(readOnly = true)
	@Query("""
			SELECT vto FROM VetTimeOff vto JOIN FETCH vto.vet
			WHERE vto.vet.id = :vetId AND vto.date BETWEEN :startDate AND :endDate
			ORDER BY vto.date ASC
			""")
	List<VetTimeOff> findByVetIdAndDateBetween(@Param("vetId") Integer vetId, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Transactional(readOnly = true)
	@Query("SELECT vto FROM VetTimeOff vto JOIN FETCH vto.vet WHERE vto.vet.id = :vetId ORDER BY vto.date ASC")
	List<VetTimeOff> findByVetId(@Param("vetId") Integer vetId);

	@Transactional(readOnly = true)
	@Query("SELECT vto FROM VetTimeOff vto JOIN FETCH vto.vet WHERE vto.date = :date")
	List<VetTimeOff> findByDate(@Param("date") LocalDate date);

}
