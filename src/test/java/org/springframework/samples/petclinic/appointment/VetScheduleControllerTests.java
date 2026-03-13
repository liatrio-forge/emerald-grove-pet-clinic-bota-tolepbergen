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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for {@link VetScheduleController}.
 *
 * <p>
 * Uses standaloneSetup to avoid requiring Thymeleaf templates on the classpath. This
 * keeps the backend agent's scope clean: no template files are created by these tests.
 */
class VetScheduleControllerTests {

	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.systemDefault());

	private static final int TEST_VET_ID = 1;

	private static final int TEST_TIME_OFF_ID = 10;

	private MockMvc mockMvc;

	@Mock
	private VetScheduleService vetScheduleService;

	@Mock
	private VetRepository vetRepo;

	@Mock
	private ClinicScheduleConfigRepository clinicConfigRepo;

	@Mock
	private MessageSource messageSource;

	private Vet testVet;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		VetScheduleController controller = new VetScheduleController(vetScheduleService, vetRepo, clinicConfigRepo,
				messageSource, FIXED_CLOCK);

		// Use InternalResourceViewResolver so standaloneSetup does not fail on
		// view name resolution during redirect assertions.
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".html");

		mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();

		testVet = new Vet();
		testVet.setId(TEST_VET_ID);
		testVet.setFirstName("James");
		testVet.setLastName("Carter");

		given(vetRepo.findById(TEST_VET_ID)).willReturn(Optional.of(testVet));
		given(vetScheduleService.getWeeklySchedule(TEST_VET_ID)).willReturn(Collections.emptyList());
		given(vetScheduleService.getTimeOff(TEST_VET_ID)).willReturn(Collections.emptyList());
		given(clinicConfigRepo.findAllByOrderByDayOfWeekAsc()).willReturn(Collections.emptyList());

		// Set up MessageSource mock to return expected flash messages
		given(messageSource.getMessage(eq("schedule.saved"), nullable(Object[].class), any(java.util.Locale.class)))
			.willReturn("Schedule saved successfully.");
		given(messageSource.getMessage(eq("timeoff.added"), nullable(Object[].class), any(java.util.Locale.class)))
			.willReturn("Time off added successfully.");
		given(messageSource.getMessage(eq("timeoff.addedWithWarning"), any(Object[].class),
				any(java.util.Locale.class)))
			.willReturn("Time off added, but there are 2 existing appointment(s) on this date that need rescheduling.");
		given(messageSource.getMessage(eq("timeoff.removed"), nullable(Object[].class), any(java.util.Locale.class)))
			.willReturn("Time off removed successfully.");
		given(messageSource.getMessage(eq("timeoff.duplicate"), nullable(Object[].class), any(java.util.Locale.class)))
			.willReturn("Time off already exists for this date.");
	}

	// -------------------------------------------------------------------------
	// GET /vets/{vetId}/schedule
	// -------------------------------------------------------------------------

	@Test
	void showScheduleExisting() throws Exception {
		VetSchedule schedule = new VetSchedule();
		schedule.setDayOfWeek(1);
		schedule.setStartTime(LocalTime.of(9, 0));
		schedule.setEndTime(LocalTime.of(17, 0));
		schedule.setIsAvailable(true);

		VetTimeOff timeOff1 = new VetTimeOff();
		timeOff1.setDate(LocalDate.now(FIXED_CLOCK).plusDays(5));
		VetTimeOff timeOff2 = new VetTimeOff();
		timeOff2.setDate(LocalDate.now(FIXED_CLOCK).plusDays(10));

		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setDayOfWeek(1);

		given(vetScheduleService.getWeeklySchedule(TEST_VET_ID)).willReturn(List.of(schedule));
		given(vetScheduleService.getTimeOff(TEST_VET_ID)).willReturn(List.of(timeOff1, timeOff2));
		given(clinicConfigRepo.findAllByOrderByDayOfWeekAsc()).willReturn(List.of(config));

		mockMvc.perform(get("/vets/{vetId}/schedule", TEST_VET_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("vets/vetSchedule"))
			.andExpect(model().attribute("vet", equalTo(testVet)))
			.andExpect(model().attribute("schedules", hasSize(1)))
			.andExpect(model().attribute("timeOffs", hasSize(2)))
			.andExpect(model().attributeExists("clinicConfigs"))
			.andExpect(model().attributeExists("daysOfWeek"));
	}

	@Test
	void showScheduleNotFound() throws Exception {
		given(vetRepo.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/vets/{vetId}/schedule", 999)).andExpect(status().isNotFound());
	}

	@Test
	void testShowVetSchedule_ModelContainsSchedules() throws Exception {
		// Arrange
		VetSchedule schedule = new VetSchedule();
		schedule.setDayOfWeek(1);
		schedule.setStartTime(LocalTime.of(9, 0));
		schedule.setEndTime(LocalTime.of(17, 0));
		schedule.setIsAvailable(true);

		given(vetScheduleService.getWeeklySchedule(TEST_VET_ID)).willReturn(List.of(schedule));

		// Act & Assert
		mockMvc.perform(get("/vets/{vetId}/schedule", TEST_VET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("schedules"))
			.andExpect(model().attribute("schedules", hasSize(1)));
	}

	@Test
	void testShowVetSchedule_ModelContainsTimeOffs() throws Exception {
		// Arrange
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setDate(LocalDate.of(2026, 4, 6));

		given(vetScheduleService.getTimeOff(TEST_VET_ID)).willReturn(List.of(timeOff));

		// Act & Assert
		mockMvc.perform(get("/vets/{vetId}/schedule", TEST_VET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("timeOffs"))
			.andExpect(model().attribute("timeOffs", hasSize(1)));
	}

	@Test
	void testShowVetSchedule_WithTimeOff_ShowsInTimeOffs() throws Exception {
		// Arrange - Helen Leary's time off on 2026-04-06
		VetTimeOff helenTimeOff = new VetTimeOff();
		helenTimeOff.setDate(LocalDate.of(2026, 4, 6));

		given(vetScheduleService.getTimeOff(TEST_VET_ID)).willReturn(List.of(helenTimeOff));

		// Act & Assert
		mockMvc.perform(get("/vets/{vetId}/schedule", TEST_VET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("timeOffs", hasSize(1)));
	}

	// -------------------------------------------------------------------------
	// POST /vets/{vetId}/schedule
	// -------------------------------------------------------------------------

	@Test
	@SuppressWarnings("unchecked")
	void updateScheduleSuccess() throws Exception {
		VetSchedule saved = new VetSchedule();
		saved.setDayOfWeek(1);
		saved.setStartTime(LocalTime.of(9, 0));
		saved.setEndTime(LocalTime.of(17, 0));
		saved.setIsAvailable(true);
		given(vetScheduleService.updateWeekSchedule(eq(TEST_VET_ID), any(Map.class))).willReturn(List.of(saved));

		mockMvc
			.perform(post("/vets/{vetId}/schedule", TEST_VET_ID).param("day_1_start", "09:00")
				.param("day_1_end", "17:00")
				.param("day_1_available", "true")
				.param("day_2_start", "09:00")
				.param("day_2_end", "17:00")
				.param("day_2_available", "true")
				.param("day_3_start", "09:00")
				.param("day_3_end", "17:00")
				.param("day_3_available", "true")
				.param("day_4_start", "09:00")
				.param("day_4_end", "17:00")
				.param("day_4_available", "true")
				.param("day_5_start", "09:00")
				.param("day_5_end", "17:00")
				.param("day_5_available", "true")
				.param("day_6_start", "09:00")
				.param("day_6_end", "12:00")
				.param("day_6_available", "true"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attribute("message", containsString("saved")));
		verify(vetScheduleService).updateWeekSchedule(eq(TEST_VET_ID), any(Map.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	void updateScheduleInvalidTimes() throws Exception {
		given(vetScheduleService.updateWeekSchedule(eq(TEST_VET_ID), any(Map.class)))
			.willThrow(new IllegalArgumentException("Day 1: Start time must be before end time"));

		mockMvc
			.perform(post("/vets/{vetId}/schedule", TEST_VET_ID).param("day_1_start", "17:00")
				.param("day_1_end", "09:00")
				.param("day_1_available", "true"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attributeExists("error"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void updateScheduleOutsideClinicHours() throws Exception {
		given(vetScheduleService.updateWeekSchedule(eq(TEST_VET_ID), any(Map.class)))
			.willThrow(new IllegalArgumentException("Day 1: Vet schedule times must fall within clinic hours"));

		mockMvc
			.perform(post("/vets/{vetId}/schedule", TEST_VET_ID).param("day_1_start", "07:00")
				.param("day_1_end", "17:00")
				.param("day_1_available", "true"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attributeExists("error"));
	}

	// -------------------------------------------------------------------------
	// POST /vets/{vetId}/timeoff
	// -------------------------------------------------------------------------

	@Test
	void addTimeOffSuccess() throws Exception {
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setDate(LocalDate.of(2026, 4, 20));
		TimeOffResult result = new TimeOffResult(timeOff, Collections.emptyList());

		given(vetScheduleService.addTimeOff(eq(TEST_VET_ID), eq(LocalDate.of(2026, 4, 20)), anyString()))
			.willReturn(result);

		mockMvc
			.perform(post("/vets/{vetId}/timeoff", TEST_VET_ID).param("date", "2026-04-20")
				.param("reason", "Conference"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attribute("message", containsString("added")));
	}

	@Test
	void addTimeOffWithWarning() throws Exception {
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setDate(LocalDate.of(2026, 4, 20));

		Appointment apt1 = new Appointment();
		Appointment apt2 = new Appointment();
		TimeOffResult result = new TimeOffResult(timeOff, List.of(apt1, apt2));

		given(vetScheduleService.addTimeOff(eq(TEST_VET_ID), eq(LocalDate.of(2026, 4, 20)), isNull()))
			.willReturn(result);

		mockMvc.perform(post("/vets/{vetId}/timeoff", TEST_VET_ID).param("date", "2026-04-20"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attributeExists("conflictWarning"));
	}

	@Test
	void addTimeOffPastDate() throws Exception {
		given(vetScheduleService.addTimeOff(eq(TEST_VET_ID), any(LocalDate.class), isNull()))
			.willThrow(new IllegalArgumentException("Cannot add time off for past dates"));

		mockMvc.perform(post("/vets/{vetId}/timeoff", TEST_VET_ID).param("date", "2025-01-01"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attributeExists("error"));
	}

	@Test
	void addTimeOffDuplicate() throws Exception {
		given(vetScheduleService.addTimeOff(eq(TEST_VET_ID), any(LocalDate.class), isNull()))
			.willThrow(new DataIntegrityViolationException("duplicate entry"));

		mockMvc.perform(post("/vets/{vetId}/timeoff", TEST_VET_ID).param("date", "2026-04-20"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attribute("error", "Time off already exists for this date."));
	}

	// -------------------------------------------------------------------------
	// POST /vets/{vetId}/timeoff/{id}/delete
	// -------------------------------------------------------------------------

	@Test
	void removeTimeOffSuccess() throws Exception {
		mockMvc.perform(post("/vets/{vetId}/timeoff/{id}/delete", TEST_VET_ID, TEST_TIME_OFF_ID))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attribute("message", containsString("removed")));
		verify(vetScheduleService).removeTimeOff(TEST_VET_ID, TEST_TIME_OFF_ID);
	}

	@Test
	void removeTimeOffNotFound() throws Exception {
		willThrow(new ResourceNotFoundException("VetTimeOff not found with id: 999")).given(vetScheduleService)
			.removeTimeOff(TEST_VET_ID, 999);

		mockMvc.perform(post("/vets/{vetId}/timeoff/{id}/delete", TEST_VET_ID, 999))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/vets/" + TEST_VET_ID + "/schedule"))
			.andExpect(flash().attributeExists("error"));
	}

	// -------------------------------------------------------------------------
	// Cross-cutting: GET /vets/{vetId}/timeoff should return 405
	// -------------------------------------------------------------------------

	@Test
	void getTimeOffReturns405() throws Exception {
		mockMvc.perform(get("/vets/{vetId}/timeoff", TEST_VET_ID)).andExpect(status().isMethodNotAllowed());
	}

	// -------------------------------------------------------------------------
	// TICKET-06: today and textStyleFull model attribute tests
	// -------------------------------------------------------------------------

	@Test
	void testShowVetSchedule_ModelContainsTodayAndTextStyleFull() throws Exception {
		mockMvc.perform(get("/vets/{vetId}/schedule", TEST_VET_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("today", LocalDate.now(FIXED_CLOCK).toString()))
			.andExpect(model().attribute("textStyleFull", java.time.format.TextStyle.FULL));
	}

}
