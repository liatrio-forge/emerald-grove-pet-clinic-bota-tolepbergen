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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests for {@link ScheduleViewController}.
 *
 * <p>
 * Uses standalone MockMvc setup to avoid requiring Thymeleaf templates (which are outside
 * backend scope). This verifies controller logic and model attributes only.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleViewControllerTests {

	private MockMvc mockMvc;

	@Mock
	private AppointmentRepository appointmentRepo;

	@Mock
	private AvailabilityService availabilityService;

	@Mock
	private ClinicScheduleConfigRepository clinicConfigRepo;

	@Mock
	private VetScheduleRepository vetScheduleRepo;

	@Mock
	private VetTimeOffRepository vetTimeOffRepo;

	@Mock
	private VetRepository vetRepo;

	@Mock
	private MessageSource messageSource;

	private Vet vet1;

	private Vet vet2;

	private Vet vet3;

	private ClinicScheduleConfig mondayConfig;

	private ClinicScheduleConfig sundayConfig;

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

		mondayConfig = new ClinicScheduleConfig();
		mondayConfig.setId(1);
		mondayConfig.setDayOfWeek(1); // Monday
		mondayConfig.setOpenTime(LocalTime.of(9, 0));
		mondayConfig.setCloseTime(LocalTime.of(17, 0));
		mondayConfig.setSlotDurationMinutes(30);
		mondayConfig.setIsOpen(true);

		sundayConfig = new ClinicScheduleConfig();
		sundayConfig.setId(7);
		sundayConfig.setDayOfWeek(7); // Sunday
		sundayConfig.setOpenTime(LocalTime.of(9, 0));
		sundayConfig.setCloseTime(LocalTime.of(17, 0));
		sundayConfig.setSlotDurationMinutes(30);
		sundayConfig.setIsOpen(false);

		ScheduleViewController controller = new ScheduleViewController(appointmentRepo, availabilityService,
				clinicConfigRepo, vetScheduleRepo, vetTimeOffRepo, vetRepo, messageSource);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		// Default stub for vet.notFound message
		given(messageSource.getMessage(eq("vet.notFound"), nullable(Object[].class), any(Locale.class)))
			.willReturn("Vet not found.");

		// Default stubs for vetRepo
		given(vetRepo.findAll()).willReturn(List.of(vet1, vet2, vet3));
		given(vetRepo.findById(1)).willReturn(Optional.of(vet1));
		given(vetRepo.findById(2)).willReturn(Optional.of(vet2));
		given(vetRepo.findById(3)).willReturn(Optional.of(vet3));
		given(vetRepo.findById(999)).willReturn(Optional.empty());

		// Default stubs - Monday open
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(sundayConfig));
		given(clinicConfigRepo.findAllByOrderByDayOfWeekAsc()).willReturn(List.of(mondayConfig, sundayConfig));

		// Default: no time off
		given(vetTimeOffRepo.findByDate(any(LocalDate.class))).willReturn(List.of());
		given(vetTimeOffRepo.findByVetIdAndDateBetween(any(), any(), any())).willReturn(List.of());

		// Default: no vet schedules
		given(vetScheduleRepo.findAvailableByDayOfWeek(any())).willReturn(List.of());
		given(vetScheduleRepo.findByVetId(any())).willReturn(List.of());

		// Default: no appointments
		given(appointmentRepo.findByDate(any(LocalDate.class))).willReturn(List.of());
		given(appointmentRepo.findByVetIdAndDateBetween(any(), any(), any())).willReturn(List.of());
	}

	// =========================================================================
	// GET /appointments/schedule (Daily View)
	// =========================================================================

	@Test
	void dailyDefaultDate() throws Exception {
		// No date param => defaults to today
		given(clinicConfigRepo.findByDayOfWeek(LocalDate.now().getDayOfWeek().getValue()))
			.willReturn(Optional.of(mondayConfig));

		mockMvc.perform(get("/appointments/schedule"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("currentDate", LocalDate.now()))
			.andExpect(model().attribute("isToday", true));
	}

	@Test
	void dailySpecificDate() throws Exception {
		LocalDate monday = LocalDate.of(2026, 4, 6);

		mockMvc.perform(get("/appointments/schedule").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("currentDate", monday));
	}

	@Test
	void dailyClosedDay() throws Exception {
		// Sunday 2026-04-12 is closed — no time slots or vet columns should be shown
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(sundayConfig));

		mockMvc.perform(get("/appointments/schedule").param("date", "2026-04-12"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("clinicClosed", true))
			.andExpect(model().attribute("timeSlots", hasSize(0)))
			.andExpect(model().attribute("availableVets", hasSize(0)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void dailyWithAppointments() throws Exception {
		LocalDate monday = LocalDate.of(2026, 4, 6);

		Appointment appt1 = buildAppointment(1, vet1, monday, LocalTime.of(9, 0), LocalTime.of(9, 30));
		Appointment appt2 = buildAppointment(2, vet1, monday, LocalTime.of(10, 0), LocalTime.of(10, 30));
		Appointment appt3 = buildAppointment(3, vet3, monday, LocalTime.of(11, 0), LocalTime.of(11, 30));

		given(appointmentRepo.findByDate(monday)).willReturn(List.of(appt1, appt2, appt3));
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));

		mockMvc.perform(get("/appointments/schedule").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("appointmentsByVet"))
			.andExpect(model().attribute("appointmentsByVet", allOf(hasKey(1), hasKey(3))));
	}

	@Test
	void dailyVetsFiltered() throws Exception {
		LocalDate monday = LocalDate.of(2026, 4, 6);

		// 3 vets scheduled to work but vet2 has time off
		VetSchedule schedule1 = buildVetSchedule(vet1, 1);
		VetSchedule schedule2 = buildVetSchedule(vet2, 1);
		VetSchedule schedule3 = buildVetSchedule(vet3, 1);

		given(vetScheduleRepo.findAvailableByDayOfWeek(1)).willReturn(List.of(schedule1, schedule2, schedule3));
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));

		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setVet(vet2);
		timeOff.setDate(monday);
		given(vetTimeOffRepo.findByDate(monday)).willReturn(List.of(timeOff));

		mockMvc.perform(get("/appointments/schedule").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("availableVets", hasSize(2)));
	}

	@Test
	void dailyTimeSlots() throws Exception {
		// Monday 2026-04-06, clinic open 09:00-17:00, 30-min slots => 16 slots
		given(clinicConfigRepo.findByDayOfWeek(1)).willReturn(Optional.of(mondayConfig));

		mockMvc.perform(get("/appointments/schedule").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("timeSlots", hasSize(16)));
	}

	@Test
	void dailyNavigation() throws Exception {
		LocalDate monday = LocalDate.of(2026, 4, 6);

		mockMvc.perform(get("/appointments/schedule").param("date", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("prevDate", monday.minusDays(1)))
			.andExpect(model().attribute("nextDate", monday.plusDays(1)));
	}

	@Test
	void dailyModelAttributes() throws Exception {
		given(clinicConfigRepo.findByDayOfWeek(LocalDate.now().getDayOfWeek().getValue()))
			.willReturn(Optional.of(mondayConfig));

		mockMvc.perform(get("/appointments/schedule"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("currentDate"))
			.andExpect(model().attributeExists("date"))
			.andExpect(model().attributeExists("timeSlots"))
			.andExpect(model().attributeExists("availableVets"))
			.andExpect(model().attributeExists("vets"))
			.andExpect(model().attributeExists("appointmentsByVet"))
			.andExpect(model().attributeExists("appointmentGrid"))
			.andExpect(model().attributeExists("slotSpans"))
			.andExpect(model().attributeExists("skippedSlots"))
			.andExpect(model().attributeExists("prevDate"))
			.andExpect(model().attributeExists("nextDate"))
			.andExpect(model().attributeExists("isToday"));
	}

	@Test
	void dailyViewName() throws Exception {
		mockMvc.perform(get("/appointments/schedule"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/dailySchedule"));
	}

	// =========================================================================
	// GET /appointments/schedule/weekly (Weekly View)
	// =========================================================================

	@Test
	void weeklyDefaultVetAndWeek() throws Exception {
		LocalDate today = LocalDate.now();
		LocalDate expectedWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

		mockMvc.perform(get("/appointments/schedule/weekly"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("selectedVet", notNullValue()))
			.andExpect(model().attribute("weekStart", expectedWeekStart));
	}

	@Test
	void weeklySpecificVet() throws Exception {
		mockMvc.perform(get("/appointments/schedule/weekly").param("vetId", "3"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("selectedVet", vet3));
	}

	@Test
	void weeklySpecificWeek() throws Exception {
		LocalDate weekStart = LocalDate.of(2026, 4, 6); // Monday

		mockMvc.perform(get("/appointments/schedule/weekly").param("week", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("weekStart", weekStart))
			.andExpect(model().attribute("weekDays", hasSize(7)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void weeklyWithAppointments() throws Exception {
		LocalDate weekStart = LocalDate.of(2026, 4, 6);
		LocalDate weekEnd = weekStart.plusDays(6);

		Appointment appt1 = buildAppointment(1, vet1, LocalDate.of(2026, 4, 6), LocalTime.of(9, 0),
				LocalTime.of(9, 30));
		Appointment appt2 = buildAppointment(2, vet1, LocalDate.of(2026, 4, 6), LocalTime.of(10, 0),
				LocalTime.of(10, 30));
		Appointment appt3 = buildAppointment(3, vet1, LocalDate.of(2026, 4, 6), LocalTime.of(11, 0),
				LocalTime.of(11, 30));
		Appointment appt4 = buildAppointment(4, vet1, LocalDate.of(2026, 4, 8), LocalTime.of(9, 0),
				LocalTime.of(9, 30));
		Appointment appt5 = buildAppointment(5, vet1, LocalDate.of(2026, 4, 8), LocalTime.of(10, 0),
				LocalTime.of(10, 30));

		given(appointmentRepo.findByVetIdAndDateBetween(1, weekStart, weekEnd))
			.willReturn(List.of(appt1, appt2, appt3, appt4, appt5));

		mockMvc.perform(get("/appointments/schedule/weekly").param("vetId", "1").param("week", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("appointmentsByDay"))
			.andExpect(model().attribute("appointmentsByDay",
					allOf(hasKey(LocalDate.of(2026, 4, 6)), hasKey(LocalDate.of(2026, 4, 8)))));
	}

	@Test
	void weeklyTimeOffShown() throws Exception {
		LocalDate weekStart = LocalDate.of(2026, 4, 6);
		LocalDate weekEnd = weekStart.plusDays(6);

		LocalDate timeOff1Date = LocalDate.of(2026, 4, 7);
		LocalDate timeOff2Date = LocalDate.of(2026, 4, 9);

		VetTimeOff vto1 = new VetTimeOff();
		vto1.setVet(vet1);
		vto1.setDate(timeOff1Date);

		VetTimeOff vto2 = new VetTimeOff();
		vto2.setVet(vet1);
		vto2.setDate(timeOff2Date);

		given(vetTimeOffRepo.findByVetIdAndDateBetween(1, weekStart, weekEnd)).willReturn(List.of(vto1, vto2));

		mockMvc.perform(get("/appointments/schedule/weekly").param("vetId", "1").param("week", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("timeOffDates", containsInAnyOrder(timeOff1Date, timeOff2Date)));
	}

	@Test
	void weeklyClosedDayShown() throws Exception {
		// Week 2026-04-06 to 2026-04-12 includes Sunday (day-of-week 7) which is closed.
		// clinicConfigs map must have key 7 with isOpen=false.
		given(clinicConfigRepo.findByDayOfWeek(7)).willReturn(Optional.of(sundayConfig));

		mockMvc.perform(get("/appointments/schedule/weekly").param("vetId", "1").param("week", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("clinicConfigs", hasEntry(is(7), hasProperty("isOpen", is(false)))));
	}

	@Test
	void weeklyAllVetsInDropdown() throws Exception {
		given(vetRepo.findAll()).willReturn(List.of(vet1, vet2, vet3));

		mockMvc.perform(get("/appointments/schedule/weekly"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("allVets", hasSize(3)));
	}

	@Test
	void weeklyNavigation() throws Exception {
		LocalDate weekStart = LocalDate.of(2026, 4, 6);

		mockMvc.perform(get("/appointments/schedule/weekly").param("week", "2026-04-06"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("prevWeek", weekStart.minusWeeks(1)))
			.andExpect(model().attribute("nextWeek", weekStart.plusWeeks(1)));
	}

	@Test
	void weeklyViewName() throws Exception {
		mockMvc.perform(get("/appointments/schedule/weekly"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/weeklySchedule"));
	}

	@Test
	void weeklyVetNotFound() throws Exception {
		// vetId=999 does not exist; controller should redirect without vetId param
		given(vetRepo.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/appointments/schedule/weekly").param("vetId", "999"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/schedule/weekly"))
			.andExpect(flash().attribute("error", "Vet not found."));
	}

	// =========================================================================
	// TICKET-06: textStyleShort model attribute test
	// =========================================================================

	@Test
	void testWeeklyView_ModelContainsTextStyleShort() throws Exception {
		mockMvc.perform(get("/appointments/schedule/weekly"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("textStyleShort", java.time.format.TextStyle.SHORT));
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	private Appointment buildAppointment(int id, Vet vet, LocalDate date, LocalTime start, LocalTime end) {
		Appointment appt = new Appointment();
		appt.setId(id);
		appt.setVet(vet);
		appt.setAppointmentDate(date);
		appt.setStartTime(start);
		appt.setEndTime(end);
		return appt;
	}

	private VetSchedule buildVetSchedule(Vet vet, int dayOfWeek) {
		VetSchedule vs = new VetSchedule();
		vs.setVet(vet);
		vs.setDayOfWeek(dayOfWeek);
		vs.setStartTime(LocalTime.of(9, 0));
		vs.setEndTime(LocalTime.of(17, 0));
		vs.setIsAvailable(true);
		return vs;
	}

}
