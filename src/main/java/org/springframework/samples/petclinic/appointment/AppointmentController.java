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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web controller for appointment booking and CRUD operations.
 *
 * <p>
 * Exposes five endpoints:
 * <ul>
 * <li>GET /appointments/new — booking form with optional pre-fill params</li>
 * <li>POST /appointments/new — create appointment, handle conflicts</li>
 * <li>GET /appointments/{id} — detail view with status transition flags</li>
 * <li>GET /appointments/{id}/edit — edit form (only SCHEDULED/CONFIRMED)</li>
 * <li>POST /appointments/{id}/edit — update appointment, handle conflicts</li>
 * </ul>
 */
@Controller
@RequestMapping("/appointments")
class AppointmentController {

	private static final String VIEW_CREATE_FORM = "appointments/createAppointmentForm";

	private static final String VIEW_EDIT_FORM = "appointments/editAppointmentForm";

	private static final String VIEW_DETAILS = "appointments/appointmentDetails";

	private final AppointmentService appointmentService;

	private final AppointmentTypeRepository appointmentTypeRepo;

	private final OwnerRepository ownerRepo;

	private final VetRepository vetRepo;

	private final AvailabilityService availabilityService;

	private final Clock clock;

	public AppointmentController(AppointmentService appointmentService, AppointmentTypeRepository appointmentTypeRepo,
			OwnerRepository ownerRepo, VetRepository vetRepo, AvailabilityService availabilityService, Clock clock) {
		this.appointmentService = appointmentService;
		this.appointmentTypeRepo = appointmentTypeRepo;
		this.ownerRepo = ownerRepo;
		this.vetRepo = vetRepo;
		this.availabilityService = availabilityService;
		this.clock = clock;
	}

	// -------------------------------------------------------------------------
	// GET /appointments/new
	// -------------------------------------------------------------------------

	/**
	 * Renders the new appointment booking form.
	 *
	 * <p>
	 * Accepts optional query params to pre-fill the form from a calendar click or pet
	 * page link.
	 */
	@GetMapping("/new")
	public String initNewAppointmentForm(@RequestParam(required = false) Integer petId,
			@RequestParam(required = false) Integer vetId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
			Model model) {

		model.addAttribute("appointment", new Appointment());
		model.addAttribute("appointmentTypes", appointmentTypeRepo.findAllWithSpecialty());

		List<Owner> owners = ownerRepo.findAll();
		model.addAttribute("owners", owners);

		List<Pet> pets = owners.stream().flatMap(o -> o.getPets().stream()).toList();
		model.addAttribute("pets", pets);

		model.addAttribute("vets", vetRepo.findAll());
		model.addAttribute("timeSlots", List.of());
		model.addAttribute("today", LocalDate.now(clock).toString());

		if (petId != null) {
			model.addAttribute("prefilledPetId", petId);
			// Resolve owner from the already-loaded owners list — avoids a second DB
			// query
			owners.stream()
				.filter(o -> o.getPets().stream().anyMatch(p -> petId.equals(p.getId())))
				.findFirst()
				.ifPresent(owner -> model.addAttribute("prefilledOwnerId", owner.getId()));
		}

		if (vetId != null) {
			model.addAttribute("prefilledVetId", vetId);
		}

		if (date != null) {
			model.addAttribute("prefilledDate", date);
		}

		if (time != null) {
			model.addAttribute("prefilledTime", time);
		}

		return VIEW_CREATE_FORM;
	}

	// -------------------------------------------------------------------------
	// POST /appointments/new
	// -------------------------------------------------------------------------

	/**
	 * Processes the new appointment form submission.
	 *
	 * <p>
	 * On success redirects to the appointment detail view. On
	 * {@link SchedulingConflictException} or {@link ResourceNotFoundException} re-renders
	 * the form with error information.
	 */
	@PostMapping("/new")
	public String processNewAppointmentForm(@RequestParam Integer petId, @RequestParam Integer ownerId,
			@RequestParam Integer vetId, @RequestParam Integer appointmentTypeId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam(required = false) String notes, Model model, RedirectAttributes redirectAttributes) {

		try {
			Appointment appointment = appointmentService.createAppointment(petId, ownerId, vetId, appointmentTypeId,
					date, startTime, notes);
			redirectAttributes.addFlashAttribute("message", "Appointment booked successfully.");
			return "redirect:/appointments/" + appointment.getId();
		}
		catch (SchedulingConflictException ex) {
			model.addAttribute("conflicts", ex.getConflictResult().conflicts());
			repopulateNewFormModel(model, petId, ownerId, vetId, appointmentTypeId, date, startTime, notes);
			return VIEW_CREATE_FORM;
		}
		catch (ResourceNotFoundException ex) {
			model.addAttribute("error", ex.getMessage());
			repopulateNewFormModel(model, petId, ownerId, vetId, appointmentTypeId, date, startTime, notes);
			return VIEW_CREATE_FORM;
		}
	}

	// -------------------------------------------------------------------------
	// GET /appointments/{id}
	// -------------------------------------------------------------------------

	/**
	 * Displays the appointment detail view with status transition flags.
	 * @throws ResourceNotFoundException if no appointment with the given id exists
	 */
	@GetMapping("/{id}")
	public ModelAndView showAppointment(@PathVariable Integer id) {
		Appointment appointment = appointmentService.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

		ModelAndView mav = new ModelAndView(VIEW_DETAILS);
		mav.addObject("appointment", appointment);

		AppointmentStatus status = appointment.getStatus();
		mav.addObject("canConfirm", status.canTransitionTo(AppointmentStatus.CONFIRMED));
		mav.addObject("canCancel", status.canTransitionTo(AppointmentStatus.CANCELLED));
		mav.addObject("canComplete", status.canTransitionTo(AppointmentStatus.COMPLETED));
		mav.addObject("canEdit", status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED);

		return mav;
	}

	// -------------------------------------------------------------------------
	// GET /appointments/{id}/edit
	// -------------------------------------------------------------------------

	/**
	 * Displays the edit form for an existing appointment.
	 *
	 * <p>
	 * Only SCHEDULED and CONFIRMED appointments can be edited. CANCELLED or COMPLETED
	 * appointments redirect to the detail view with an error flash message.
	 * @throws ResourceNotFoundException if no appointment with the given id exists
	 */
	@GetMapping("/{id}/edit")
	public String initEditAppointmentForm(@PathVariable Integer id, Model model,
			RedirectAttributes redirectAttributes) {

		Appointment appointment = appointmentService.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

		AppointmentStatus status = appointment.getStatus();
		if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED) {
			redirectAttributes.addFlashAttribute("error", "Cannot edit appointment in " + status.name() + " status.");
			return "redirect:/appointments/" + id;
		}

		model.addAttribute("appointment", appointment);
		model.addAttribute("appointmentTypes", appointmentTypeRepo.findAllWithSpecialty());
		model.addAttribute("vets", vetRepo.findAll());
		model.addAttribute("timeSlots",
				getSlotsIncludingCurrent(appointment.getVet().getId(), appointment.getAppointmentDate(),
						appointment.getAppointmentType().getDefaultDurationMinutes(), appointment.getStartTime(),
						appointment.getEndTime()));
		model.addAttribute("today", LocalDate.now(clock).toString());
		return VIEW_EDIT_FORM;
	}

	// -------------------------------------------------------------------------
	// POST /appointments/{id}/edit
	// -------------------------------------------------------------------------

	/**
	 * Processes the appointment edit form submission.
	 *
	 * <p>
	 * On success redirects to the detail view. On {@link SchedulingConflictException}
	 * re-renders the edit form with conflict errors. On {@link IllegalStateException}
	 * redirects to the detail view with an error flash message.
	 */
	@PostMapping("/{id}/edit")
	public String processEditAppointmentForm(@PathVariable Integer id, @RequestParam Integer vetId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam(required = false) String notes, Model model, RedirectAttributes redirectAttributes) {

		try {
			appointmentService.updateAppointment(id, vetId, date, startTime, notes);
			redirectAttributes.addFlashAttribute("message", "Appointment updated successfully.");
			return "redirect:/appointments/" + id;
		}
		catch (SchedulingConflictException ex) {
			model.addAttribute("conflicts", ex.getConflictResult().conflicts());
			// Re-populate form with the user's submitted values so they are not lost.
			// Load the persisted appointment only to retain stable read-only fields
			// (pet, appointmentType, status, id, createdAt) then overlay the submitted
			// editable fields on top so the form re-renders what the user typed.
			Optional<Appointment> reloaded = appointmentService.findById(id);
			reloaded.ifPresent(a -> {
				repopulateEditFormModel(a, vetId, date, startTime, notes);
				model.addAttribute("appointment", a);
			});
			int durationMinutes = reloaded.map(a -> a.getAppointmentType().getDefaultDurationMinutes()).orElse(30);
			model.addAttribute("appointmentTypes", appointmentTypeRepo.findAllWithSpecialty());
			model.addAttribute("vets", vetRepo.findAll());
			model.addAttribute("timeSlots", getSlotsIncludingCurrent(vetId, date, durationMinutes, startTime,
					startTime.plusMinutes(durationMinutes)));
			model.addAttribute("today", LocalDate.now(clock).toString());
			return VIEW_EDIT_FORM;
		}
		catch (IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/appointments/" + id;
		}
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	/**
	 * Returns available time slots for the given vet and date, always including the
	 * current appointment's slot even if it is already occupied by this appointment.
	 *
	 * <p>
	 * This ensures the edit form pre-selects the existing time without showing it as
	 * unavailable.
	 * @param vetId the vet ID to query
	 * @param date the appointment date
	 * @param durationMinutes the appointment duration in minutes
	 * @param currentStart the current appointment start time (always included)
	 * @param currentEnd the current appointment end time (used to build the current slot)
	 * @return list of {@link TimeSlot}s that includes the current slot
	 */
	private List<TimeSlot> getSlotsIncludingCurrent(Integer vetId, LocalDate date, int durationMinutes,
			LocalTime currentStart, LocalTime currentEnd) {
		List<TimeSlot> slots = new ArrayList<>(availabilityService.getAvailableSlots(vetId, date, durationMinutes));
		TimeSlot currentSlot = new TimeSlot(currentStart, currentEnd);
		if (!slots.contains(currentSlot)) {
			slots.add(0, currentSlot);
		}
		return slots;
	}

	/**
	 * Overlays the user's submitted editable fields onto a loaded {@link Appointment} so
	 * that the edit form re-renders the submitted values rather than the persisted ones.
	 *
	 * <p>
	 * Only the four user-editable fields (date, startTime, vet, notes) are mutated. The
	 * stable read-only fields (pet, appointmentType, status, id, createdAt) remain
	 * unchanged, which ensures the form action URL and read-only display values are still
	 * correct.
	 */
	private void repopulateEditFormModel(Appointment appointment, Integer vetId, LocalDate date, LocalTime startTime,
			String notes) {
		appointment.setAppointmentDate(date);
		appointment.setStartTime(startTime);
		appointment.setNotes(notes);
		vetRepo.findById(vetId).ifPresent(appointment::setVet);
	}

	/**
	 * Repopulates the new appointment form model with submitted values after an error.
	 */
	private void repopulateNewFormModel(Model model, Integer petId, Integer ownerId, Integer vetId,
			Integer appointmentTypeId, LocalDate date, LocalTime startTime, String notes) {
		Appointment appointment = new Appointment();
		appointment.setAppointmentDate(date);
		appointment.setStartTime(startTime);
		appointment.setNotes(notes);
		model.addAttribute("appointment", appointment);
		model.addAttribute("appointmentTypes", appointmentTypeRepo.findAllWithSpecialty());
		List<Owner> owners = ownerRepo.findAll();
		model.addAttribute("owners", owners);
		List<Pet> pets = owners.stream().flatMap(o -> o.getPets().stream()).toList();
		model.addAttribute("pets", pets);
		model.addAttribute("vets", vetRepo.findAll());
		model.addAttribute("today", LocalDate.now(clock).toString());
		model.addAttribute("prefilledPetId", petId);
		model.addAttribute("prefilledOwnerId", ownerId);
		model.addAttribute("prefilledVetId", vetId);
		model.addAttribute("prefilledAppointmentTypeId", appointmentTypeId);
		model.addAttribute("prefilledDate", date);
		model.addAttribute("prefilledTime", startTime);
		model.addAttribute("prefilledNotes", notes);
	}

}
