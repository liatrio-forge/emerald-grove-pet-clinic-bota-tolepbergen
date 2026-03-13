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
 * Repository for {@link AppointmentType} entities.
 *
 * <p>
 * All query methods use JOIN FETCH for {@code requiredSpecialty} because
 * {@code spring.jpa.open-in-view=false} -- callers need specialty loaded eagerly.
 */
public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Integer> {

	@Transactional(readOnly = true)
	Optional<AppointmentType> findByName(String name);

	@Transactional(readOnly = true)
	@Query("SELECT at FROM AppointmentType at LEFT JOIN FETCH at.requiredSpecialty")
	List<AppointmentType> findAllWithSpecialty();

	@Transactional(readOnly = true)
	@Query("SELECT at FROM AppointmentType at LEFT JOIN FETCH at.requiredSpecialty WHERE at.id = :id")
	Optional<AppointmentType> findByIdWithSpecialty(@Param("id") Integer id);

}
