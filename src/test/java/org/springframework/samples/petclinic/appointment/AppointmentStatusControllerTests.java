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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link AppointmentStatusController}.
 *
 * <p>
 * Covers confirm, cancel, and complete status transitions including success, invalid
 * state, and not-found scenarios.
 */
@WebMvcTest(AppointmentStatusController.class)
@DisabledInNativeImage
@DisabledInAotMode
class AppointmentStatusControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AppointmentService appointmentService;

	// -------------------------------------------------------------------------
	// POST /appointments/{id}/confirm
	// -------------------------------------------------------------------------

	@Test
	void shouldRedirectToDetailsWithSuccessMessageWhenConfirmingAppointment() throws Exception {
		// Arrange
		given(appointmentService.confirmAppointment(1)).willReturn(null);

		// Act & Assert
		mockMvc.perform(post("/appointments/1/confirm"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/1"))
			.andExpect(flash().attribute("message", "Appointment confirmed."));

		then(appointmentService).should().confirmAppointment(1);
	}

	@Test
	void shouldRedirectWithErrorWhenConfirmingAppointmentInInvalidState() throws Exception {
		// Arrange
		String errorMsg = "Cannot transition from CANCELLED to CONFIRMED. Invalid state transition.";
		willThrow(new IllegalStateException(errorMsg)).given(appointmentService).confirmAppointment(1);

		// Act & Assert
		mockMvc.perform(post("/appointments/1/confirm"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/1"))
			.andExpect(flash().attribute("error", errorMsg));
	}

	@Test
	void shouldRedirectToListWithErrorWhenConfirmingNonExistentAppointment() throws Exception {
		// Arrange
		willThrow(new ResourceNotFoundException("Appointment not found with id: 9999")).given(appointmentService)
			.confirmAppointment(9999);

		// Act & Assert — not-found redirects to the appointments list (the detail page
		// would also 404, so the flash message must be shown somewhere reachable)
		mockMvc.perform(post("/appointments/9999/confirm"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/find"))
			.andExpect(flash().attribute("error", "Appointment not found."));
	}

	// -------------------------------------------------------------------------
	// POST /appointments/{id}/cancel
	// -------------------------------------------------------------------------

	@Test
	void shouldRedirectToDetailsWithSuccessMessageWhenCancellingScheduledAppointment() throws Exception {
		// Arrange
		given(appointmentService.cancelAppointment(1)).willReturn(null);

		// Act & Assert
		mockMvc.perform(post("/appointments/1/cancel"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/1"))
			.andExpect(flash().attribute("message", "Appointment cancelled."));

		then(appointmentService).should().cancelAppointment(1);
	}

	@Test
	void shouldRedirectToDetailsWithSuccessMessageWhenCancellingConfirmedAppointment() throws Exception {
		// Arrange
		given(appointmentService.cancelAppointment(2)).willReturn(null);

		// Act & Assert
		mockMvc.perform(post("/appointments/2/cancel"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/2"))
			.andExpect(flash().attribute("message", "Appointment cancelled."));

		then(appointmentService).should().cancelAppointment(2);
	}

	@Test
	void shouldRedirectWithErrorWhenCancellingAppointmentInInvalidState() throws Exception {
		// Arrange
		String errorMsg = "Cannot transition from COMPLETED to CANCELLED. Invalid state transition.";
		willThrow(new IllegalStateException(errorMsg)).given(appointmentService).cancelAppointment(1);

		// Act & Assert
		mockMvc.perform(post("/appointments/1/cancel"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/1"))
			.andExpect(flash().attribute("error", errorMsg));
	}

	@Test
	void shouldRedirectToListWithErrorWhenCancellingNonExistentAppointment() throws Exception {
		// Arrange
		willThrow(new ResourceNotFoundException("Appointment not found with id: 9999")).given(appointmentService)
			.cancelAppointment(9999);

		// Act & Assert — not-found redirects to the appointments list
		mockMvc.perform(post("/appointments/9999/cancel"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/find"))
			.andExpect(flash().attribute("error", "Appointment not found."));
	}

	// -------------------------------------------------------------------------
	// POST /appointments/{id}/complete
	// -------------------------------------------------------------------------

	@Test
	void shouldRedirectToDetailsWithSuccessMessageWhenCompletingConfirmedAppointment() throws Exception {
		// Arrange
		given(appointmentService.completeAppointment(1)).willReturn(null);

		// Act & Assert
		mockMvc.perform(post("/appointments/1/complete"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/1"))
			.andExpect(flash().attribute("message", "Appointment completed. Visit record created."));

		then(appointmentService).should().completeAppointment(1);
	}

	@Test
	void shouldRedirectToDetailsWithSuccessMessageWhenCompletingScheduledAppointment() throws Exception {
		// Arrange
		given(appointmentService.completeAppointment(2)).willReturn(null);

		// Act & Assert
		mockMvc.perform(post("/appointments/2/complete"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/2"))
			.andExpect(flash().attribute("message", "Appointment completed. Visit record created."));

		then(appointmentService).should().completeAppointment(2);
	}

	@Test
	void shouldRedirectWithErrorWhenCompletingAppointmentInInvalidState() throws Exception {
		// Arrange
		String errorMsg = "Cannot transition from CANCELLED to COMPLETED. Invalid state transition.";
		willThrow(new IllegalStateException(errorMsg)).given(appointmentService).completeAppointment(1);

		// Act & Assert
		mockMvc.perform(post("/appointments/1/complete"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/1"))
			.andExpect(flash().attribute("error", errorMsg));
	}

	@Test
	void shouldRedirectToListWithErrorWhenCompletingNonExistentAppointment() throws Exception {
		// Arrange
		willThrow(new ResourceNotFoundException("Appointment not found with id: 9999")).given(appointmentService)
			.completeAppointment(9999);

		// Act & Assert — not-found redirects to the appointments list
		mockMvc.perform(post("/appointments/9999/complete"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/find"))
			.andExpect(flash().attribute("error", "Appointment not found."));
	}

	// -------------------------------------------------------------------------
	// Cross-cutting: GET returns 405 Method Not Allowed
	// -------------------------------------------------------------------------

	@Test
	void shouldReturnMethodNotAllowedWhenConfirmEndpointCalledWithGet() throws Exception {
		mockMvc.perform(get("/appointments/1/confirm")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	void shouldReturnMethodNotAllowedWhenCancelEndpointCalledWithGet() throws Exception {
		mockMvc.perform(get("/appointments/1/cancel")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	void shouldReturnMethodNotAllowedWhenCompleteEndpointCalledWithGet() throws Exception {
		mockMvc.perform(get("/appointments/1/complete")).andExpect(status().isMethodNotAllowed());
	}

	// -------------------------------------------------------------------------
	// Redirect URL contains appointment ID
	// -------------------------------------------------------------------------

	@Test
	void shouldIncludeAppointmentIdInRedirectUrlAfterSuccessfulConfirm() throws Exception {
		// Arrange
		given(appointmentService.confirmAppointment(42)).willReturn(null);

		// Act & Assert
		mockMvc.perform(post("/appointments/42/confirm"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/appointments/42"));

		then(appointmentService).should().confirmAppointment(42);
	}

}
