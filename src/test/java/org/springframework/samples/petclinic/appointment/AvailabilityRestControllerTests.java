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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link AvailabilityRestController}.
 *
 * <p>
 * Uses {@link WebMvcTest} to load only the web layer with a mocked
 * {@link AvailabilityService}.
 */
@WebMvcTest(AvailabilityRestController.class)
@DisabledInNativeImage
@DisabledInAotMode
class AvailabilityRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AvailabilityService availabilityService;

	private static final LocalDate TEST_DATE = LocalDate.of(2026, 4, 6);

	private Vet vet1;

	private Vet vet2;

	private Vet vet3;

	@BeforeEach
	void setUp() {
		vet1 = new Vet();
		vet1.setId(1);
		vet1.setFirstName("James");
		vet1.setLastName("Carter");

		vet2 = new Vet();
		vet2.setId(2);
		vet2.setFirstName("Helen");
		vet2.setLastName("Leary");

		vet3 = new Vet();
		vet3.setId(3);
		vet3.setFirstName("Linda");
		vet3.setLastName("Douglas");
		Specialty surgery = new Specialty();
		surgery.setId(2);
		surgery.setName("surgery");
		Specialty dentistry = new Specialty();
		dentistry.setId(1);
		dentistry.setName("dentistry");
		vet3.addSpecialty(surgery);
		vet3.addSpecialty(dentistry);
	}

	// -----------------------------------------------------------------------
	// GET /api/vets/available
	// -----------------------------------------------------------------------

	@Test
	void availableVetsReturned() throws Exception {
		// Arrange
		given(availabilityService.getAvailableVets(TEST_DATE, null)).willReturn(List.of(vet1, vet2, vet3));

		// Act & Assert
		mockMvc.perform(get("/api/vets/available").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(3)));
	}

	@Test
	void noVetsAvailable() throws Exception {
		// Arrange
		given(availabilityService.getAvailableVets(TEST_DATE, null)).willReturn(List.of());

		// Act & Assert
		mockMvc.perform(get("/api/vets/available").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void filterBySpecialty() throws Exception {
		// Arrange: only vet3 has surgery specialty (id=2)
		given(availabilityService.getAvailableVets(TEST_DATE, 2)).willReturn(List.of(vet3));

		// Act & Assert
		mockMvc.perform(get("/api/vets/available").param("date", "2026-04-06").param("specialtyId", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].lastName", is("Douglas")));
	}

	@Test
	void noSpecialtyFilter() throws Exception {
		// Arrange: specialtyId not provided -> service called with null
		given(availabilityService.getAvailableVets(TEST_DATE, null)).willReturn(List.of(vet1, vet2));

		// Act & Assert
		mockMvc.perform(get("/api/vets/available").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)));

		verify(availabilityService).getAvailableVets(TEST_DATE, null);
	}

	@Test
	void missingDateParam() throws Exception {
		// Act & Assert: no date param -> 400
		mockMvc.perform(get("/api/vets/available")).andExpect(status().isBadRequest());
	}

	@Test
	void invalidDateFormat() throws Exception {
		// Act & Assert: invalid date format -> 400
		mockMvc.perform(get("/api/vets/available").param("date", "not-a-date")).andExpect(status().isBadRequest());
	}

	@Test
	void jsonStructureForVetAvailabilityDto() throws Exception {
		// Arrange: vet3 has surgery and dentistry
		given(availabilityService.getAvailableVets(TEST_DATE, null)).willReturn(List.of(vet3));

		// Act & Assert: verify JSON fields
		mockMvc.perform(get("/api/vets/available").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id", is(3)))
			.andExpect(jsonPath("$[0].firstName", is("Linda")))
			.andExpect(jsonPath("$[0].lastName", is("Douglas")))
			.andExpect(jsonPath("$[0].specialties", hasSize(2)));
	}

	// -----------------------------------------------------------------------
	// GET /api/slots/available
	// -----------------------------------------------------------------------

	@Test
	void availableSlotsReturned() throws Exception {
		// Arrange: 5 open slots
		List<TimeSlot> slots = List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30)),
				new TimeSlot(LocalTime.of(9, 30), LocalTime.of(10, 0)),
				new TimeSlot(LocalTime.of(10, 0), LocalTime.of(10, 30)),
				new TimeSlot(LocalTime.of(10, 30), LocalTime.of(11, 0)),
				new TimeSlot(LocalTime.of(11, 0), LocalTime.of(11, 30)));
		given(availabilityService.getAvailableSlots(1, TEST_DATE, 30)).willReturn(slots);

		// Act & Assert
		mockMvc.perform(get("/api/slots/available").param("vetId", "1").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(5)));
	}

	@Test
	void noSlotsAvailable() throws Exception {
		// Arrange: fully booked
		given(availabilityService.getAvailableSlots(1, TEST_DATE, 30)).willReturn(List.of());

		// Act & Assert
		mockMvc.perform(get("/api/slots/available").param("vetId", "1").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void defaultDuration() throws Exception {
		// Arrange: no durationMinutes param -> service called with 30
		given(availabilityService.getAvailableSlots(1, TEST_DATE, 30)).willReturn(List.of());

		// Act
		mockMvc.perform(get("/api/slots/available").param("vetId", "1").param("date", "2026-04-06"))
			.andExpect(status().isOk());

		// Assert: service was called with default 30
		verify(availabilityService).getAvailableSlots(1, TEST_DATE, 30);
	}

	@Test
	void customDuration() throws Exception {
		// Arrange: durationMinutes=90 -> passed to service
		given(availabilityService.getAvailableSlots(1, TEST_DATE, 90)).willReturn(List.of());

		// Act
		mockMvc
			.perform(get("/api/slots/available").param("vetId", "1")
				.param("date", "2026-04-06")
				.param("durationMinutes", "90"))
			.andExpect(status().isOk());

		// Assert: service was called with 90
		verify(availabilityService).getAvailableSlots(1, TEST_DATE, 90);
	}

	@Test
	void missingVetId() throws Exception {
		// Act & Assert: no vetId -> 400
		mockMvc.perform(get("/api/slots/available").param("date", "2026-04-06")).andExpect(status().isBadRequest());
	}

	@Test
	void missingDate() throws Exception {
		// Act & Assert: no date -> 400
		mockMvc.perform(get("/api/slots/available").param("vetId", "1")).andExpect(status().isBadRequest());
	}

	@Test
	void jsonStructureForTimeSlotDto() throws Exception {
		// Arrange: one slot 09:00-09:30
		List<TimeSlot> slots = List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30)));
		given(availabilityService.getAvailableSlots(1, TEST_DATE, 30)).willReturn(slots);

		// Act & Assert
		mockMvc.perform(get("/api/slots/available").param("vetId", "1").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].startTime", is("09:00")))
			.andExpect(jsonPath("$[0].endTime", is("09:30")));
	}

	// -----------------------------------------------------------------------
	// Malformed numeric parameters
	// -----------------------------------------------------------------------

	@Test
	void shouldReturnBadRequestWhenSpecialtyIdIsNonNumeric() throws Exception {
		// Act & Assert: non-numeric specialtyId -> 400
		mockMvc.perform(get("/api/vets/available").param("date", "2026-04-06").param("specialtyId", "abc"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnBadRequestWhenVetIdIsNonNumeric() throws Exception {
		// Act & Assert: non-numeric vetId -> 400
		mockMvc.perform(get("/api/slots/available").param("vetId", "abc").param("date", "2026-04-06"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnBadRequestWhenDurationMinutesIsNonNumeric() throws Exception {
		// Act & Assert: non-numeric durationMinutes -> 400
		mockMvc
			.perform(get("/api/slots/available").param("vetId", "1")
				.param("date", "2026-04-06")
				.param("durationMinutes", "abc"))
			.andExpect(status().isBadRequest());
	}

}
