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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller that provides model data for the daily and weekly appointment schedule
 * views.
 *
 * <p>
 * The daily view ({@code GET /appointments/schedule}) shows all available vets as columns
 * with time slots as rows. The weekly view ({@code GET /appointments/schedule/weekly})
 * shows 7 days for a single selected vet.
 */
@Controller
@RequestMapping("/appointments/schedule")
class ScheduleViewController {

	private static final DateTimeFormatter SLOT_FMT = DateTimeFormatter.ofPattern("HH:mm");

	private final AppointmentRepository appointmentRepo;

	private final AvailabilityService availabilityService;

	private final ClinicScheduleConfigRepository clinicConfigRepo;

	private final VetScheduleRepository vetScheduleRepo;

	private final VetTimeOffRepository vetTimeOffRepo;

	private final VetRepository vetRepo;

	/**
	 * Value object carrying per-day status for the weekly calendar template.
	 */
	static class DayInfo {

		private final LocalDate date;

		private final DayOfWeek dayOfWeek;

		private final boolean isOpen;

		private final boolean isTimeOff;

		DayInfo(LocalDate date, DayOfWeek dayOfWeek, boolean isOpen, boolean isTimeOff) {
			this.date = date;
			this.dayOfWeek = dayOfWeek;
			this.isOpen = isOpen;
			this.isTimeOff = isTimeOff;
		}

		public LocalDate getDate() {
			return date;
		}

		public DayOfWeek getDayOfWeek() {
			return dayOfWeek;
		}

		public boolean getIsOpen() {
			return isOpen;
		}

		public boolean getIsTimeOff() {
			return isTimeOff;
		}

	}

	public ScheduleViewController(AppointmentRepository appointmentRepo, AvailabilityService availabilityService,
			ClinicScheduleConfigRepository clinicConfigRepo, VetScheduleRepository vetScheduleRepo,
			VetTimeOffRepository vetTimeOffRepo, VetRepository vetRepo) {
		this.appointmentRepo = appointmentRepo;
		this.availabilityService = availabilityService;
		this.clinicConfigRepo = clinicConfigRepo;
		this.vetScheduleRepo = vetScheduleRepo;
		this.vetTimeOffRepo = vetTimeOffRepo;
		this.vetRepo = vetRepo;
	}

	/**
	 * GET /appointments/schedule
	 *
	 * <p>
	 * Daily view: shows all vets as columns, time slots as rows.
	 * @param date the date to display; defaults to today
	 * @param model the Spring MVC model
	 * @return the view name
	 */
	@GetMapping
	public String dailyView(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			Model model) {

		LocalDate currentDate = (date != null) ? date : LocalDate.now();
		boolean isToday = currentDate.equals(LocalDate.now());

		// "currentDate" for test backward-compat; "date" for template
		model.addAttribute("currentDate", currentDate);
		model.addAttribute("date", currentDate);
		model.addAttribute("isToday", isToday);
		model.addAttribute("prevDate", currentDate.minusDays(1));
		model.addAttribute("nextDate", currentDate.plusDays(1));

		// Look up clinic config for this day of week
		int dayOfWeek = currentDate.getDayOfWeek().getValue();
		Optional<ClinicScheduleConfig> configOpt = clinicConfigRepo.findByDayOfWeek(dayOfWeek);

		boolean clinicClosed = configOpt.isEmpty() || !Boolean.TRUE.equals(configOpt.get().getIsOpen());
		model.addAttribute("clinicClosed", clinicClosed);
		model.addAttribute("clinicConfig", configOpt.orElse(null));

		if (clinicClosed) {
			model.addAttribute("timeSlots", List.of());
			model.addAttribute("availableVets", List.of());
			model.addAttribute("vets", List.of());
			model.addAttribute("appointmentsByVet", Map.of());
			model.addAttribute("appointmentGrid", Map.of());
			model.addAttribute("slotSpans", Map.of());
			model.addAttribute("skippedSlots", Set.of());
			return "appointments/dailySchedule";
		}

		ClinicScheduleConfig config = configOpt.get();
		int slotDurationMinutes = config.getSlotDurationMinutes();

		// Generate time slots as LocalTime values (template uses #temporals.format)
		List<LocalTime> timeSlots = generateTimeSlots(config.getOpenTime(), config.getCloseTime(), slotDurationMinutes);
		model.addAttribute("timeSlots", timeSlots);

		// Scheduled vets for this day of week
		List<VetSchedule> scheduledVets = vetScheduleRepo.findAvailableByDayOfWeek(dayOfWeek);
		List<VetTimeOff> timeOffList = vetTimeOffRepo.findByDate(currentDate);
		Set<Integer> vetsOnTimeOff = timeOffList.stream().map(vto -> vto.getVet().getId()).collect(Collectors.toSet());

		List<Vet> availableVets = scheduledVets.stream()
			.filter(vs -> !vetsOnTimeOff.contains(vs.getVet().getId()))
			.map(VetSchedule::getVet)
			.collect(Collectors.toList());
		// "availableVets" for test backward-compat; "vets" for template
		model.addAttribute("availableVets", availableVets);

		// Appointments grouped by vet id (for test backward-compat)
		List<Appointment> appointments = appointmentRepo.findByDate(currentDate);
		Map<Integer, List<Appointment>> appointmentsByVet = appointments.stream()
			.collect(Collectors.groupingBy(a -> a.getVet().getId()));
		model.addAttribute("appointmentsByVet", appointmentsByVet);

		// "vets" for template: available vets PLUS any time-off vet with existing
		// appointments
		// (so staff can see and reschedule appointments belonging to absent vets)
		Set<Integer> availableVetIds = availableVets.stream().map(Vet::getId).collect(Collectors.toSet());
		List<Vet> vetsForGrid = new ArrayList<>(availableVets);
		for (Appointment appt : appointments) {
			Vet apptVet = appt.getVet();
			if (!availableVetIds.contains(apptVet.getId())) {
				availableVetIds.add(apptVet.getId());
				vetsForGrid.add(apptVet);
			}
		}
		model.addAttribute("vets", vetsForGrid);

		// appointmentGrid for template: keyed by "{vetId}-HH:mm"
		GridData dailyGrid = buildAppointmentGrid(appointments, appt -> String.valueOf(appt.getVet().getId()),
				slotDurationMinutes);
		model.addAttribute("appointmentGrid", dailyGrid.grid());
		model.addAttribute("slotSpans", dailyGrid.spans());
		model.addAttribute("skippedSlots", dailyGrid.skipped());

		return "appointments/dailySchedule";
	}

	/**
	 * GET /appointments/schedule/weekly
	 *
	 * <p>
	 * Weekly view: shows 7 days for one vet.
	 * @param vetId the vet to display; defaults to the first available vet
	 * @param week any date within the target week; normalized to its Monday
	 * @param model the Spring MVC model
	 * @return the view name
	 */
	@GetMapping("/weekly")
	public String weeklyView(@RequestParam(required = false) Integer vetId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week, Model model,
			RedirectAttributes redirectAttributes) {

		// Resolve week start (always Monday) — normalize any supplied date to its Monday
		LocalDate weekStart = (week != null) ? week.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
				: LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate weekEnd = weekStart.plusDays(6);

		// All vets for dropdown
		Collection<Vet> allVetsCollection = vetRepo.findAll();
		List<Vet> allVets = new ArrayList<>(allVetsCollection);
		model.addAttribute("allVets", allVets);

		// Resolve selected vet — search in-memory to reuse the already-loaded allVets
		// list
		Vet selectedVet = null;
		if (vetId != null) {
			Optional<Vet> vetOpt = allVets.stream().filter(v -> vetId.equals(v.getId())).findFirst();
			if (vetOpt.isPresent()) {
				selectedVet = vetOpt.get();
			}
			else {
				redirectAttributes.addFlashAttribute("error", "Vet not found.");
				return "redirect:/appointments/schedule/weekly";
			}
		}
		else {
			selectedVet = allVets.isEmpty() ? null : allVets.get(0);
		}

		// "selectedVet" for test backward-compat; "vet" for template
		model.addAttribute("selectedVet", selectedVet);
		model.addAttribute("vet", selectedVet);
		model.addAttribute("weekStart", weekStart);
		model.addAttribute("prevWeek", weekStart.minusWeeks(1));
		model.addAttribute("nextWeek", weekStart.plusWeeks(1));

		// Build weekDays list (7 days Mon-Sun)
		List<LocalDate> weekDays = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			weekDays.add(weekStart.plusDays(i));
		}
		model.addAttribute("weekDays", weekDays);

		// Clinic configs for each day of week — single query instead of one per day
		Map<Integer, ClinicScheduleConfig> clinicConfigs = new LinkedHashMap<>();
		for (ClinicScheduleConfig cfg : clinicConfigRepo.findAllByOrderByDayOfWeekAsc()) {
			clinicConfigs.put(cfg.getDayOfWeek(), cfg);
		}
		model.addAttribute("clinicConfigs", clinicConfigs);

		// Vet schedules for selected vet (by day of week)
		Map<Integer, VetSchedule> vetSchedules = new LinkedHashMap<>();
		if (selectedVet != null) {
			List<VetSchedule> scheduleList = vetScheduleRepo.findByVetId(selectedVet.getId());
			for (VetSchedule vs : scheduleList) {
				vetSchedules.put(vs.getDayOfWeek(), vs);
			}
		}
		model.addAttribute("vetSchedules", vetSchedules);

		// Time off dates for selected vet within the week
		Set<LocalDate> timeOffDates = new HashSet<>();
		if (selectedVet != null) {
			List<VetTimeOff> timeOffList = vetTimeOffRepo.findByVetIdAndDateBetween(selectedVet.getId(), weekStart,
					weekEnd);
			for (VetTimeOff vto : timeOffList) {
				timeOffDates.add(vto.getDate());
			}
		}
		model.addAttribute("timeOffDates", timeOffDates);

		// Build "days" list for template with per-day open/time-off status
		List<DayInfo> days = new ArrayList<>();
		for (LocalDate day : weekDays) {
			int dow = day.getDayOfWeek().getValue();
			ClinicScheduleConfig cfg = clinicConfigs.get(dow);
			boolean open = cfg != null && Boolean.TRUE.equals(cfg.getIsOpen());
			boolean timeOff = timeOffDates.contains(day);
			days.add(new DayInfo(day, day.getDayOfWeek(), open, timeOff));
		}
		model.addAttribute("days", days);

		// Generate time slots (widest hours, smallest slot duration across open days)
		int slotDuration = computeMinSlotDuration(clinicConfigs);
		List<LocalTime> timeSlots = generateWeeklyTimeSlots(clinicConfigs, slotDuration);
		model.addAttribute("timeSlots", timeSlots);

		// Appointments by date (for test backward-compat)
		List<Appointment> weeklyAppointments = selectedVet != null
				? appointmentRepo.findByVetIdAndDateBetween(selectedVet.getId(), weekStart, weekEnd) : List.of();
		Map<LocalDate, List<Appointment>> appointmentsByDay = new LinkedHashMap<>();
		for (Appointment appt : weeklyAppointments) {
			appointmentsByDay.computeIfAbsent(appt.getAppointmentDate(), k -> new ArrayList<>()).add(appt);
		}
		model.addAttribute("appointmentsByDay", appointmentsByDay);

		// appointmentGrid for template: keyed by "{date}-HH:mm"
		GridData weeklyGrid = buildAppointmentGrid(weeklyAppointments,
				appt -> String.valueOf(appt.getAppointmentDate()), slotDuration);
		model.addAttribute("appointmentGrid", weeklyGrid.grid());
		model.addAttribute("slotSpans", weeklyGrid.spans());
		model.addAttribute("skippedSlots", weeklyGrid.skipped());
		model.addAttribute("textStyleShort", TextStyle.SHORT);

		return "appointments/weeklySchedule";
	}

	/**
	 * Carries the three appointment-grid maps produced by {@link #buildAppointmentGrid}.
	 */
	private record GridData(Map<String, Appointment> grid, Map<String, Integer> spans, Set<String> skipped) {
	}

	/**
	 * Builds the appointment grid maps used by the daily and weekly schedule templates.
	 *
	 * <p>
	 * Keys are composed as {@code {prefix}-HH:mm} where prefix is derived from
	 * {@code keyPrefixExtractor}.
	 * @param appointments the appointments to index
	 * @param keyPrefixExtractor produces the per-appointment key prefix (e.g. vetId or
	 * date)
	 * @param slotDurationMinutes clinic slot size, used to compute row spans
	 * @return a {@link GridData} carrying the grid, span, and skip maps
	 */
	private GridData buildAppointmentGrid(List<Appointment> appointments,
			java.util.function.Function<Appointment, String> keyPrefixExtractor, int slotDurationMinutes) {
		Map<String, Appointment> grid = new LinkedHashMap<>();
		Map<String, Integer> spans = new LinkedHashMap<>();
		Set<String> skipped = new HashSet<>();
		for (Appointment appt : appointments) {
			String key = keyPrefixExtractor.apply(appt) + "-" + appt.getStartTime().format(SLOT_FMT);
			grid.put(key, appt);
			long durationMins = Duration.between(appt.getStartTime(), appt.getEndTime()).toMinutes();
			int span = (int) Math.max(1, Math.ceil((double) durationMins / slotDurationMinutes));
			if (span > 1) {
				spans.put(key, span);
				for (int s = 1; s < span; s++) {
					skipped.add(keyPrefixExtractor.apply(appt) + "-"
							+ appt.getStartTime().plusMinutes((long) s * slotDurationMinutes).format(SLOT_FMT));
				}
			}
		}
		return new GridData(grid, spans, skipped);
	}

	/**
	 * Generates time slot start times from openTime to (closeTime - slotDuration) in
	 * increments of slotDuration.
	 * @param openTime clinic open time
	 * @param closeTime clinic close time
	 * @param slotDurationMinutes slot size in minutes
	 * @return list of slot start times as {@link LocalTime} values
	 */
	private List<LocalTime> generateTimeSlots(LocalTime openTime, LocalTime closeTime, int slotDurationMinutes) {
		if (slotDurationMinutes <= 0) {
			return List.of();
		}
		List<LocalTime> slots = new ArrayList<>();
		LocalTime current = openTime;
		while (current.plusMinutes(slotDurationMinutes).compareTo(closeTime) <= 0) {
			slots.add(current);
			current = current.plusMinutes(slotDurationMinutes);
		}
		return slots;
	}

	/**
	 * Computes the minimum slot duration across all open days in the week's configs.
	 * @param clinicConfigs map of day-of-week to clinic config
	 * @return minimum slot duration in minutes, or 30 if no open days found
	 */
	private int computeMinSlotDuration(Map<Integer, ClinicScheduleConfig> clinicConfigs) {
		int minDuration = Integer.MAX_VALUE;
		for (ClinicScheduleConfig cfg : clinicConfigs.values()) {
			if (Boolean.TRUE.equals(cfg.getIsOpen())) {
				minDuration = Math.min(minDuration, cfg.getSlotDurationMinutes());
			}
		}
		return minDuration == Integer.MAX_VALUE ? 30 : minDuration;
	}

	/**
	 * Generates time slots using the widest open hours across the week's clinic configs.
	 * Falls back to a default 09:00-17:00 range if no open days found.
	 * @param clinicConfigs map of day-of-week to clinic config
	 * @param slotDuration slot duration in minutes (minimum across open days)
	 * @return list of slot start times
	 */
	private List<LocalTime> generateWeeklyTimeSlots(Map<Integer, ClinicScheduleConfig> clinicConfigs,
			int slotDuration) {
		LocalTime earliest = null;
		LocalTime latest = null;

		for (ClinicScheduleConfig cfg : clinicConfigs.values()) {
			if (Boolean.TRUE.equals(cfg.getIsOpen())) {
				if (earliest == null || cfg.getOpenTime().isBefore(earliest)) {
					earliest = cfg.getOpenTime();
				}
				if (latest == null || cfg.getCloseTime().isAfter(latest)) {
					latest = cfg.getCloseTime();
				}
			}
		}

		if (earliest == null) {
			earliest = LocalTime.of(9, 0);
			latest = LocalTime.of(17, 0);
		}

		return generateTimeSlots(earliest, latest, slotDuration);
	}

}
