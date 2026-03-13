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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web controller for managing veterinarian weekly schedules and time-off dates.
 *
 * <p>
 * Provides endpoints to view the vet schedule page, update the weekly schedule (all 7
 * days submitted as a form), add a time-off date, and remove a time-off entry.
 */
@Controller
class VetScheduleController {

	private final VetScheduleService vetScheduleService;

	private final VetRepository vetRepo;

	private final ClinicScheduleConfigRepository clinicConfigRepo;

	private final MessageSource messageSource;

	private final Clock clock;

	public VetScheduleController(VetScheduleService vetScheduleService, VetRepository vetRepo,
			ClinicScheduleConfigRepository clinicConfigRepo, MessageSource messageSource, Clock clock) {
		this.vetScheduleService = vetScheduleService;
		this.vetRepo = vetRepo;
		this.clinicConfigRepo = clinicConfigRepo;
		this.messageSource = messageSource;
		this.clock = clock;
	}

	/**
	 * GET /vets/{vetId}/schedule — View vet's weekly schedule and time-off list.
	 */
	@GetMapping("/vets/{vetId}/schedule")
	public String showVetSchedule(@PathVariable Integer vetId, Model model) {
		Vet vet = vetRepo.findById(vetId)
			.orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));

		List<VetSchedule> weeklySchedule = vetScheduleService.getWeeklySchedule(vetId);
		List<VetTimeOff> timeOffList = vetScheduleService.getTimeOff(vetId);
		List<ClinicScheduleConfig> clinicConfigs = clinicConfigRepo.findAllByOrderByDayOfWeekAsc();
		List<DayOfWeek> daysOfWeek = Arrays.asList(DayOfWeek.values());

		model.addAttribute("vet", vet);
		model.addAttribute("schedules", weeklySchedule);
		model.addAttribute("timeOffs", timeOffList);
		model.addAttribute("clinicConfigs", clinicConfigs);
		model.addAttribute("daysOfWeek", daysOfWeek);
		model.addAttribute("today", LocalDate.now(clock).toString());
		model.addAttribute("textStyleFull", TextStyle.FULL);

		return "vets/vetSchedule";
	}

	/**
	 * POST /vets/{vetId}/schedule — Update vet's weekly schedule (all days submitted as a
	 * form).
	 *
	 * <p>
	 * Form parameters are keyed by day: {@code day_N_start}, {@code day_N_end},
	 * {@code day_N_available} for N=1..7.
	 *
	 * <p>
	 * All days are validated and saved in a single transaction via
	 * {@link VetScheduleService#updateWeekSchedule} so that a validation error on any day
	 * prevents all days from being committed (all-or-nothing behaviour).
	 */
	@PostMapping("/vets/{vetId}/schedule")
	public String updateVetSchedule(@PathVariable Integer vetId, @RequestParam Map<String, String> formParams,
			RedirectAttributes redirectAttributes, Locale locale) {

		Map<Integer, VetScheduleService.DayScheduleRequest> daySchedules = new LinkedHashMap<>();
		for (int day = 1; day <= 7; day++) {
			String startParam = formParams.get("day_" + day + "_start");
			String endParam = formParams.get("day_" + day + "_end");
			String availableParam = formParams.get("day_" + day + "_available");

			boolean startBlank = startParam == null || startParam.isBlank();
			boolean endBlank = endParam == null || endParam.isBlank();

			if (startBlank && endBlank) {
				continue;
			}

			if (startBlank) {
				redirectAttributes.addFlashAttribute("error",
						"Day " + day + ": start time is required when end time is provided");
				return "redirect:/vets/" + vetId + "/schedule";
			}

			if (endBlank) {
				redirectAttributes.addFlashAttribute("error",
						"Day " + day + ": end time is required when start time is provided");
				return "redirect:/vets/" + vetId + "/schedule";
			}

			try {
				LocalTime startTime = LocalTime.parse(startParam);
				LocalTime endTime = LocalTime.parse(endParam);
				boolean isAvailable = "true".equalsIgnoreCase(availableParam);
				daySchedules.put(day, new VetScheduleService.DayScheduleRequest(startTime, endTime, isAvailable));
			}
			catch (DateTimeParseException ex) {
				redirectAttributes.addFlashAttribute("error", "Day " + day + ": invalid time format — use HH:mm");
				return "redirect:/vets/" + vetId + "/schedule";
			}
		}

		try {
			vetScheduleService.updateWeekSchedule(vetId, daySchedules);
			redirectAttributes.addFlashAttribute("message", messageSource.getMessage("schedule.saved", null, locale));
		}
		catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}

		return "redirect:/vets/" + vetId + "/schedule";
	}

	/**
	 * POST /vets/{vetId}/timeoff — Add a time-off date for the vet.
	 */
	@PostMapping("/vets/{vetId}/timeoff")
	public String addTimeOff(@PathVariable Integer vetId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) String reason, RedirectAttributes redirectAttributes, Locale locale) {

		try {
			TimeOffResult result = vetScheduleService.addTimeOff(vetId, date, reason);

			if (result.hasExistingAppointments()) {
				int count = result.existingAppointments().size();
				redirectAttributes.addFlashAttribute("conflictWarning",
						messageSource.getMessage("timeoff.addedWithWarning", new Object[] { count }, locale));
			}
			else {
				redirectAttributes.addFlashAttribute("message",
						messageSource.getMessage("timeoff.added", null, locale));
			}
		}
		catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		catch (DataIntegrityViolationException ex) {
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage("timeoff.duplicate", null, locale));
		}

		return "redirect:/vets/" + vetId + "/schedule";
	}

	/**
	 * POST /vets/{vetId}/timeoff/{id}/delete — Remove a time-off entry.
	 */
	@PostMapping("/vets/{vetId}/timeoff/{id}/delete")
	public String removeTimeOff(@PathVariable Integer vetId, @PathVariable Integer id,
			RedirectAttributes redirectAttributes, Locale locale) {

		try {
			vetScheduleService.removeTimeOff(vetId, id);
			redirectAttributes.addFlashAttribute("message", messageSource.getMessage("timeoff.removed", null, locale));
		}
		catch (ResourceNotFoundException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}

		return "redirect:/vets/" + vetId + "/schedule";
	}

}
