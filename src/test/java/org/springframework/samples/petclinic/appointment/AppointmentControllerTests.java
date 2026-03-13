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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.ResourceNotFoundException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Test class for {@link AppointmentController}. Uses standaloneSetup to avoid needing
 * Thymeleaf templates — templates are a frontend agent concern.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentControllerTests {

	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.systemDefault());

	private static final int TEST_APPOINTMENT_ID = 1;

	private static final int TEST_PET_ID = 1;

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_VET_ID = 1;

	private static final int TEST_APPOINTMENT_TYPE_ID = 1;

	@Mock
	private AppointmentService appointmentService;

	@Mock
	private AppointmentTypeRepository appointmentTypeRepo;

	@Mock
	private OwnerRepository ownerRepo;

	@Mock
	private VetRepository vetRepo;

	@Mock
	private AvailabilityService availabilityService;

	@Mock
	private MessageSource messageSource;

	private MockMvc mockMvc;

	private Owner testOwner;

	private Pet testPet;

	private Vet testVet;

	private AppointmentType testAppointmentType;

	private Appointment scheduledAppointment;

	private Appointment completedAppointment;

	@BeforeEach
	void setup() {
		AppointmentController controller = new AppointmentController(appointmentService, appointmentTypeRepo, ownerRepo,
				vetRepo, availabilityService, messageSource, FIXED_CLOCK);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		testPet = new Pet();
		testPet.setId(TEST_PET_ID);
		testPet.setName("Leo");

		testOwner = new Owner();
		testOwner.setId(TEST_OWNER_ID);
		testOwner.setFirstName("George");
		testOwner.setLastName("Franklin");
		testOwner.setAddress("110 W. Liberty St.");
		testOwner.setCity("Madison");
		testOwner.setTelephone("6085551023");
		testOwner.getPets().add(testPet);

		testVet = new Vet();
		testVet.setId(TEST_VET_ID);
		testVet.setFirstName("James");
		testVet.setLastName("Carter");

		testAppointmentType = new AppointmentType();
		testAppointmentType.setId(TEST_APPOINTMENT_TYPE_ID);
		testAppointmentType.setName("Checkup");
		testAppointmentType.setDefaultDurationMinutes(30);

		scheduledAppointment = new Appointment();
		scheduledAppointment.setId(TEST_APPOINTMENT_ID);
		scheduledAppointment.setPet(testPet);
		scheduledAppointment.setVet(testVet);
		scheduledAppointment.setAppointmentType(testAppointmentType);
		scheduledAppointment.setAppointmentDate(LocalDate.of(2026, 4, 10));
		scheduledAppointment.setStartTime(LocalTime.of(9, 0));
		scheduledAppointment.setEndTime(LocalTime.of(9, 30));
		scheduledAppointment.setCreatedAt(java.time.LocalDateTime.now());

		completedAppointment = new Appointment();
		completedAppointment.setId(2);
		completedAppointment.setPet(testPet);
		completedAppointment.setVet(testVet);
		completedAppointment.setAppointmentType(testAppointmentType);
		completedAppointment.setAppointmentDate(LocalDate.of(2026, 4, 5));
		completedAppointment.setStartTime(LocalTime.of(10, 0));
		completedAppointment.setEndTime(LocalTime.of(10, 30));
		completedAppointment.setCreatedAt(java.time.LocalDateTime.now());
		// Transition to COMPLETED
		completedAppointment.setStatus(AppointmentStatus.COMPLETED);
	}

	// -------------------------------------------------------------------------
	// GET /appointments/new
	// -------------------------------------------------------------------------

	@Test
	void initFormNoParams() throws Exception {
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));

		mockMvc.perform(get("/appointments/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attributeExists("appointmentTypes"));
	}

	@Test
	void initFormWithPetId() throws Exception {
		Owner otherOwner = new Owner();
		otherOwner.setId(99);
		otherOwner.setFirstName("Other");
		otherOwner.setLastName("Person");
		Pet otherPet = new Pet();
		otherPet.setId(999);
		otherOwner.addPet(otherPet);

		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(otherOwner, testOwner));

		mockMvc.perform(get("/appointments/new").param("petId", String.valueOf(TEST_PET_ID)))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attribute("prefilledPetId", TEST_PET_ID))
			.andExpect(model().attribute("prefilledOwnerId", TEST_OWNER_ID));
	}

	@Test
	void initFormWithVetIdAndDate() throws Exception {
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));

		mockMvc.perform(get("/appointments/new").param("vetId", "1").param("date", "2026-04-06").param("time", "09:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attribute("prefilledVetId", 1))
			.andExpect(model().attribute("prefilledDate", LocalDate.of(2026, 4, 6)))
			.andExpect(model().attribute("prefilledTime", LocalTime.of(9, 0)));
	}

	// -------------------------------------------------------------------------
	// POST /appointments/new
	// -------------------------------------------------------------------------

	@Test
	void createSuccess() throws Exception {
		given(appointmentService.createAppointment(eq(TEST_PET_ID), eq(TEST_OWNER_ID), eq(TEST_VET_ID),
				eq(TEST_APPOINTMENT_TYPE_ID), any(LocalDate.class), any(LocalTime.class), any()))
			.willReturn(scheduledAppointment);

		mockMvc
			.perform(post("/appointments/new").param("petId", String.valueOf(TEST_PET_ID))
				.param("ownerId", String.valueOf(TEST_OWNER_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentTypeId", String.valueOf(TEST_APPOINTMENT_TYPE_ID))
				.param("date", "2026-04-10")
				.param("startTime", "09:00"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/appointments/*"));
	}

	@Test
	void createWithConflicts() throws Exception {
		ConflictResult conflictResult = new ConflictResult(
				List.of(new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet is busy", null)));
		given(appointmentService.createAppointment(anyInt(), anyInt(), anyInt(), anyInt(), any(LocalDate.class),
				any(LocalTime.class), any()))
			.willThrow(new SchedulingConflictException(conflictResult));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));

		mockMvc
			.perform(post("/appointments/new").param("petId", String.valueOf(TEST_PET_ID))
				.param("ownerId", String.valueOf(TEST_OWNER_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentTypeId", String.valueOf(TEST_APPOINTMENT_TYPE_ID))
				.param("date", "2026-04-10")
				.param("startTime", "09:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attributeExists("conflicts"))
			.andExpect(model().attributeExists("appointmentTypes"))
			.andExpect(model().attributeExists("prefilledPetId"))
			.andExpect(model().attributeExists("prefilledOwnerId"))
			.andExpect(model().attributeExists("prefilledVetId"))
			.andExpect(model().attributeExists("prefilledAppointmentTypeId"))
			.andExpect(model().attributeExists("prefilledDate"))
			.andExpect(model().attributeExists("prefilledTime"));
	}

	@Test
	void createWithNotFound() throws Exception {
		given(appointmentService.createAppointment(anyInt(), anyInt(), anyInt(), anyInt(), any(LocalDate.class),
				any(LocalTime.class), any()))
			.willThrow(new ResourceNotFoundException("Pet not found"));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));

		mockMvc
			.perform(post("/appointments/new").param("petId", String.valueOf(TEST_PET_ID))
				.param("ownerId", String.valueOf(TEST_OWNER_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentTypeId", String.valueOf(TEST_APPOINTMENT_TYPE_ID))
				.param("date", "2026-04-10")
				.param("startTime", "09:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attributeExists("error"))
			.andExpect(model().attributeExists("appointmentTypes"))
			.andExpect(model().attributeExists("prefilledPetId"))
			.andExpect(model().attributeExists("prefilledOwnerId"))
			.andExpect(model().attributeExists("prefilledVetId"))
			.andExpect(model().attributeExists("prefilledAppointmentTypeId"))
			.andExpect(model().attributeExists("prefilledDate"))
			.andExpect(model().attributeExists("prefilledTime"));
	}

	// -------------------------------------------------------------------------
	// GET /appointments/{id}
	// -------------------------------------------------------------------------

	@Test
	void showExisting() throws Exception {
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));

		mockMvc.perform(get("/appointments/{id}", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/appointmentDetails"))
			.andExpect(model().attributeExists("appointment"))
			.andExpect(model().attributeExists("canConfirm"))
			.andExpect(model().attributeExists("canCancel"))
			.andExpect(model().attributeExists("canComplete"));
	}

	@Test
	void showNotFound() throws Exception {
		given(appointmentService.findById(9999)).willReturn(Optional.empty());

		mockMvc.perform(get("/appointments/{id}", 9999)).andExpect(status().isNotFound());
	}

	@Test
	void showScheduled() throws Exception {
		// scheduledAppointment has status SCHEDULED
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));

		mockMvc.perform(get("/appointments/{id}", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("canConfirm", true))
			.andExpect(model().attribute("canCancel", true))
			.andExpect(model().attribute("canComplete", true))
			.andExpect(model().attribute("canEdit", true));
	}

	@Test
	void showCompleted() throws Exception {
		// completedAppointment has status COMPLETED
		given(appointmentService.findById(2)).willReturn(Optional.of(completedAppointment));

		mockMvc.perform(get("/appointments/{id}", 2))
			.andExpect(status().isOk())
			.andExpect(model().attribute("canConfirm", false))
			.andExpect(model().attribute("canCancel", false))
			.andExpect(model().attribute("canComplete", false))
			.andExpect(model().attribute("canEdit", false));
	}

	// -------------------------------------------------------------------------
	// GET /appointments/{id}/edit
	// -------------------------------------------------------------------------

	@Test
	void editFormExisting() throws Exception {
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30))));

		mockMvc.perform(get("/appointments/{id}/edit", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/editAppointmentForm"))
			.andExpect(model().attributeExists("appointment"));
	}

	@Test
	void editFormCompleted() throws Exception {
		given(appointmentService.findById(2)).willReturn(Optional.of(completedAppointment));

		mockMvc.perform(get("/appointments/{id}/edit", 2))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/appointments/*"));
	}

	@Test
	void editFormNotFound() throws Exception {
		given(appointmentService.findById(9999)).willReturn(Optional.empty());

		mockMvc.perform(get("/appointments/{id}/edit", 9999)).andExpect(status().isNotFound());
	}

	@Test
	void testInitEditForm_ModelContainsVets() throws Exception {
		// Arrange
		Collection<Vet> allVets = List.of(testVet);
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(allVets);
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30))));

		// Act & Assert
		mockMvc.perform(get("/appointments/{id}/edit", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/editAppointmentForm"))
			.andExpect(model().attributeExists("vets"))
			.andExpect(model().attribute("vets", not(empty())));
	}

	@Test
	void testInitEditForm_ModelContainsTimeSlots() throws Exception {
		// Arrange
		TimeSlot currentSlot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30));
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(currentSlot));

		// Act & Assert
		mockMvc.perform(get("/appointments/{id}/edit", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("timeSlots"))
			.andExpect(model().attribute("timeSlots", not(empty())));
	}

	@Test
	void testInitEditForm_CurrentVetPreselected() throws Exception {
		// Arrange — the appointment already has testVet assigned; it should appear in
		// vets and the appointment model attribute should carry the same vet id
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30))));

		// Act & Assert — vets list contains the appointment's vet AND the appointment
		// model attribute itself has vet.id == TEST_VET_ID (pre-selection check)
		mockMvc.perform(get("/appointments/{id}/edit", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("vets", hasItem(hasProperty("id", is(TEST_VET_ID)))))
			.andExpect(model().attribute("appointment", hasProperty("vet", hasProperty("id", is(TEST_VET_ID)))));
	}

	@Test
	void testInitEditForm_CurrentSlotIncludedWhenAvailabilityReturnsEmpty() throws Exception {
		// Arrange — availability returns no slots; the controller must still include the
		// appointment's current startTime so the edit form can pre-select it
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt())).willReturn(List.of());

		// Act & Assert — timeSlots must contain a slot whose startTime matches the
		// appointment's startTime (09:00). TimeSlot is a record so we compare by value.
		TimeSlot expectedCurrentSlot = new TimeSlot(scheduledAppointment.getStartTime(),
				scheduledAppointment.getEndTime());
		mockMvc.perform(get("/appointments/{id}/edit", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("timeSlots", hasItem(expectedCurrentSlot)));
	}

	@Test
	void testProcessEditForm_ConflictRepopulatesVetsAndTimeSlots() throws Exception {
		// Arrange
		ConflictResult conflictResult = new ConflictResult(
				List.of(new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet is busy", null)));
		given(appointmentService.updateAppointment(anyInt(), anyInt(), any(LocalDate.class), any(LocalTime.class),
				any()))
			.willThrow(new SchedulingConflictException(conflictResult));
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(vetRepo.findById(TEST_VET_ID)).willReturn(Optional.of(testVet));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30))));

		// Act & Assert — on conflict, vets and timeSlots must be re-populated
		mockMvc
			.perform(post("/appointments/{id}/edit", TEST_APPOINTMENT_ID).param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", "2026-04-15")
				.param("startTime", "10:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/editAppointmentForm"))
			.andExpect(model().attributeExists("vets"))
			.andExpect(model().attributeExists("timeSlots"))
			.andExpect(model().attribute("vets", not(empty())))
			.andExpect(model().attribute("timeSlots", not(empty())));
	}

	// -------------------------------------------------------------------------
	// POST /appointments/{id}/edit
	// -------------------------------------------------------------------------

	@Test
	void editSuccess() throws Exception {
		given(appointmentService.updateAppointment(eq(TEST_APPOINTMENT_ID), eq(TEST_VET_ID), any(LocalDate.class),
				any(LocalTime.class), any()))
			.willReturn(scheduledAppointment);

		mockMvc
			.perform(post("/appointments/{id}/edit", TEST_APPOINTMENT_ID).param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", "2026-04-10")
				.param("startTime", "10:00"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/appointments/*"));
	}

	@Test
	void editWithConflicts() throws Exception {
		ConflictResult conflictResult = new ConflictResult(
				List.of(new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet is busy", null)));
		given(appointmentService.updateAppointment(anyInt(), anyInt(), any(LocalDate.class), any(LocalTime.class),
				any()))
			.willThrow(new SchedulingConflictException(conflictResult));
		// The controller reloads from DB to obtain stable read-only fields, then
		// overlays the user's submitted editable values on top.
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(vetRepo.findById(TEST_VET_ID)).willReturn(Optional.of(testVet));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(new TimeSlot(LocalTime.of(10, 0), LocalTime.of(10, 30))));

		// Submit with date=2026-04-15 and startTime=10:00 (both differ from the DB
		// fixture values of 2026-04-10 / 09:00) to verify that the user's submitted
		// values are preserved on the model rather than the persisted ones.
		mockMvc
			.perform(post("/appointments/{id}/edit", TEST_APPOINTMENT_ID).param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", "2026-04-15")
				.param("startTime", "10:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/editAppointmentForm"))
			.andExpect(model().attributeExists("conflicts"))
			.andExpect(model().attributeExists("appointmentTypes"))
			.andExpect(model().attribute("appointment", hasProperty("startTime", is(LocalTime.of(10, 0)))))
			.andExpect(model().attribute("appointment", hasProperty("appointmentDate", is(LocalDate.of(2026, 4, 15)))));
	}

	@Test
	void editIllegalState() throws Exception {
		given(appointmentService.updateAppointment(anyInt(), anyInt(), any(LocalDate.class), any(LocalTime.class),
				any()))
			.willThrow(new IllegalStateException("Cannot update cancelled appointment"));

		mockMvc
			.perform(post("/appointments/{id}/edit", TEST_APPOINTMENT_ID).param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", "2026-04-10")
				.param("startTime", "10:00"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/appointments/*"));
	}

	// -------------------------------------------------------------------------
	// BUG-1 tests: model attributes on GET /appointments/new
	// -------------------------------------------------------------------------

	@Test
	void testInitNewAppointmentForm_ReturnsOkWithModelAttributes() throws Exception {
		// Arrange
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		// Act + Assert
		mockMvc.perform(get("/appointments/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attributeExists("appointment"))
			.andExpect(model().attributeExists("owners"))
			.andExpect(model().attributeExists("pets"))
			.andExpect(model().attributeExists("vets"))
			.andExpect(model().attributeExists("timeSlots"))
			.andExpect(model().attributeExists("appointmentTypes"));
	}

	@Test
	void testInitNewAppointmentForm_WithPetIdPrefilledOwner() throws Exception {
		// Arrange
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		// Act + Assert — petId param should pre-select owner and expose owners/pets/vets
		mockMvc.perform(get("/appointments/new").param("petId", String.valueOf(TEST_PET_ID)))
			.andExpect(status().isOk())
			.andExpect(model().attribute("prefilledPetId", TEST_PET_ID))
			.andExpect(model().attribute("prefilledOwnerId", TEST_OWNER_ID))
			.andExpect(model().attributeExists("owners"))
			.andExpect(model().attributeExists("pets"))
			.andExpect(model().attributeExists("vets"));
	}

	@Test
	void testInitNewAppointmentForm_WithAllPrefillParams() throws Exception {
		// Arrange
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		// Act + Assert — vetId, date, time params should populate model
		mockMvc
			.perform(get("/appointments/new").param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", "2026-04-01")
				.param("time", "09:00"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("prefilledVetId", TEST_VET_ID))
			.andExpect(model().attribute("prefilledDate", LocalDate.of(2026, 4, 1)))
			.andExpect(model().attribute("prefilledTime", LocalTime.of(9, 0)))
			.andExpect(model().attributeExists("owners"))
			.andExpect(model().attributeExists("pets"))
			.andExpect(model().attributeExists("vets"));
	}

	@Test
	void testProcessNewAppointmentForm_ConflictRepopulatesDropdowns() throws Exception {
		// Arrange
		ConflictResult conflictResult = new ConflictResult(
				List.of(new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet is busy", null)));
		given(appointmentService.createAppointment(anyInt(), anyInt(), anyInt(), anyInt(), any(LocalDate.class),
				any(LocalTime.class), any()))
			.willThrow(new SchedulingConflictException(conflictResult));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		// Act + Assert — on conflict error, re-rendered form must include dropdown data
		mockMvc
			.perform(post("/appointments/new").param("petId", String.valueOf(TEST_PET_ID))
				.param("ownerId", String.valueOf(TEST_OWNER_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentTypeId", String.valueOf(TEST_APPOINTMENT_TYPE_ID))
				.param("date", "2026-04-10")
				.param("startTime", "09:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attributeExists("conflicts"))
			.andExpect(model().attributeExists("owners"))
			.andExpect(model().attributeExists("pets"))
			.andExpect(model().attributeExists("vets"));
	}

	// -------------------------------------------------------------------------
	// BUG-1A: appointment form-backing object on error re-render (HIGH-1)
	// -------------------------------------------------------------------------

	@Test
	void postNewAppointmentConflict_ModelContainsAppointmentAttribute() throws Exception {
		// RED: repopulateNewFormModel() does not add "appointment" — this test must fail
		// until the fix is applied.
		ConflictResult conflictResult = new ConflictResult(
				List.of(new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet is busy", null)));
		given(appointmentService.createAppointment(anyInt(), anyInt(), anyInt(), anyInt(), any(LocalDate.class),
				any(LocalTime.class), any()))
			.willThrow(new SchedulingConflictException(conflictResult));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		mockMvc
			.perform(post("/appointments/new").param("petId", String.valueOf(TEST_PET_ID))
				.param("ownerId", String.valueOf(TEST_OWNER_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentTypeId", String.valueOf(TEST_APPOINTMENT_TYPE_ID))
				.param("date", "2026-04-10")
				.param("startTime", "09:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attributeExists("appointment"));
	}

	@Test
	void postNewAppointmentNotFound_ModelContainsAppointmentAttribute() throws Exception {
		// RED: repopulateNewFormModel() does not add "appointment" — this test must fail
		// until the fix is applied.
		given(appointmentService.createAppointment(anyInt(), anyInt(), anyInt(), anyInt(), any(LocalDate.class),
				any(LocalTime.class), any()))
			.willThrow(new ResourceNotFoundException("Pet not found"));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		mockMvc
			.perform(post("/appointments/new").param("petId", String.valueOf(TEST_PET_ID))
				.param("ownerId", String.valueOf(TEST_OWNER_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentTypeId", String.valueOf(TEST_APPOINTMENT_TYPE_ID))
				.param("date", "2026-04-10")
				.param("startTime", "09:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attributeExists("appointment"));
	}

	// -------------------------------------------------------------------------
	// BUG-1B: prefilledOwnerId model key on GET /appointments/new?petId (HIGH-2)
	// -------------------------------------------------------------------------

	@Test
	void initFormWithPetId_UsesPrefilledOwnerIdNotOwner() throws Exception {
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		mockMvc.perform(get("/appointments/new").param("petId", String.valueOf(TEST_PET_ID)))
			.andExpect(status().isOk())
			.andExpect(model().attribute("prefilledOwnerId", TEST_OWNER_ID))
			.andExpect(model().attributeDoesNotExist("owner"));
	}

	// -------------------------------------------------------------------------
	// TICKET-06: today model attribute tests
	// -------------------------------------------------------------------------

	@Test
	void testInitNewAppointmentForm_ModelContainsToday() throws Exception {
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		mockMvc.perform(get("/appointments/new"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("today", LocalDate.now(FIXED_CLOCK).toString()));
	}

	@Test
	void testInitEditForm_ModelContainsToday() throws Exception {
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30))));

		mockMvc.perform(get("/appointments/{id}/edit", TEST_APPOINTMENT_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("today", LocalDate.now(FIXED_CLOCK).toString()));
	}

	@Test
	void testProcessNewForm_ConflictRepopulatesToday() throws Exception {
		ConflictResult conflictResult = new ConflictResult(
				List.of(new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet is busy", null)));
		given(appointmentService.createAppointment(anyInt(), anyInt(), anyInt(), anyInt(), any(LocalDate.class),
				any(LocalTime.class), any()))
			.willThrow(new SchedulingConflictException(conflictResult));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(ownerRepo.findAll()).willReturn(List.of(testOwner));
		given(vetRepo.findAll()).willReturn(List.of(testVet));

		mockMvc
			.perform(post("/appointments/new").param("petId", String.valueOf(TEST_PET_ID))
				.param("ownerId", String.valueOf(TEST_OWNER_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("appointmentTypeId", String.valueOf(TEST_APPOINTMENT_TYPE_ID))
				.param("date", "2026-04-10")
				.param("startTime", "09:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createAppointmentForm"))
			.andExpect(model().attribute("today", LocalDate.now(FIXED_CLOCK).toString()));
	}

	@Test
	void testProcessEditForm_ConflictRepopulatesToday() throws Exception {
		ConflictResult conflictResult = new ConflictResult(
				List.of(new SchedulingConflict(ConflictType.VET_OVERLAP, "Vet is busy", null)));
		given(appointmentService.updateAppointment(anyInt(), anyInt(), any(LocalDate.class), any(LocalTime.class),
				any()))
			.willThrow(new SchedulingConflictException(conflictResult));
		given(appointmentService.findById(TEST_APPOINTMENT_ID)).willReturn(Optional.of(scheduledAppointment));
		given(vetRepo.findById(TEST_VET_ID)).willReturn(Optional.of(testVet));
		given(appointmentTypeRepo.findAllWithSpecialty()).willReturn(List.of(testAppointmentType));
		given(vetRepo.findAll()).willReturn(List.of(testVet));
		given(availabilityService.getAvailableSlots(anyInt(), any(LocalDate.class), anyInt()))
			.willReturn(List.of(new TimeSlot(LocalTime.of(10, 0), LocalTime.of(10, 30))));

		mockMvc
			.perform(post("/appointments/{id}/edit", TEST_APPOINTMENT_ID).param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", "2026-04-15")
				.param("startTime", "10:00"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/editAppointmentForm"))
			.andExpect(model().attribute("today", LocalDate.now(FIXED_CLOCK).toString()));
	}

}
