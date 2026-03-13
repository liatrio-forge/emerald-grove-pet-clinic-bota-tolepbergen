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

import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for appointment status transitions (confirm, cancel, complete).
 *
 * <p>
 * Each endpoint accepts a POST request with the appointment ID in the path, delegates to
 * {@link AppointmentService} for the transition, and redirects to the appointment detail
 * page with an appropriate flash message.
 */
@Controller
@RequestMapping("/appointments")
class AppointmentStatusController {

	private final AppointmentService appointmentService;

	public AppointmentStatusController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	/**
	 * POST /appointments/{id}/confirm — transitions SCHEDULED to CONFIRMED.
	 * @param id the appointment ID
	 * @param redirectAttributes flash attribute holder
	 * @return redirect to appointment detail
	 */
	@PostMapping("/{id}/confirm")
	public String confirmAppointment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		try {
			appointmentService.confirmAppointment(id);
			redirectAttributes.addFlashAttribute("message", "Appointment confirmed.");
			return "redirect:/appointments/" + id;
		}
		catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/appointments/" + id;
		}
		catch (ResourceNotFoundException e) {
			redirectAttributes.addFlashAttribute("error", "Appointment not found.");
			return "redirect:/owners/find";
		}
	}

	/**
	 * POST /appointments/{id}/cancel — transitions SCHEDULED/CONFIRMED to CANCELLED.
	 * @param id the appointment ID
	 * @param redirectAttributes flash attribute holder
	 * @return redirect to appointment detail
	 */
	@PostMapping("/{id}/cancel")
	public String cancelAppointment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		try {
			appointmentService.cancelAppointment(id);
			redirectAttributes.addFlashAttribute("message", "Appointment cancelled.");
			return "redirect:/appointments/" + id;
		}
		catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/appointments/" + id;
		}
		catch (ResourceNotFoundException e) {
			redirectAttributes.addFlashAttribute("error", "Appointment not found.");
			return "redirect:/owners/find";
		}
	}

	/**
	 * POST /appointments/{id}/complete — transitions CONFIRMED/SCHEDULED to COMPLETED,
	 * creating a Visit record.
	 * @param id the appointment ID
	 * @param redirectAttributes flash attribute holder
	 * @return redirect to appointment detail
	 */
	@PostMapping("/{id}/complete")
	public String completeAppointment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		try {
			appointmentService.completeAppointment(id);
			redirectAttributes.addFlashAttribute("message", "Appointment completed. Visit record created.");
			return "redirect:/appointments/" + id;
		}
		catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/appointments/" + id;
		}
		catch (ResourceNotFoundException e) {
			redirectAttributes.addFlashAttribute("error", "Appointment not found.");
			return "redirect:/owners/find";
		}
	}

}
