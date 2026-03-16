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

import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;

/**
 * Data Transfer Object representing a vet's availability information for the REST API.
 *
 * <p>
 * Returned by {@link AvailabilityRestController#getAvailableVets}.
 *
 * @param id the vet's ID
 * @param firstName the vet's first name
 * @param lastName the vet's last name
 * @param specialties list of specialty names
 */
public record VetAvailabilityDto(Integer id, String firstName, String lastName, List<String> specialties) {

	/**
	 * Creates a {@code VetAvailabilityDto} from a {@link Vet} entity.
	 * @param vet the vet entity
	 * @return a new DTO populated from the vet
	 */
	public static VetAvailabilityDto fromVet(Vet vet) {
		return new VetAvailabilityDto(vet.getId(), vet.getFirstName(), vet.getLastName(),
				vet.getSpecialties().stream().map(Specialty::getName).toList());
	}

}
