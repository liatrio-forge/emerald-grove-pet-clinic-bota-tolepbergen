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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Unit tests for {@link Appointment} entity validation and defaults.
 */
class AppointmentTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.afterPropertiesSet();
		this.validator = localValidatorFactoryBean;
	}

	@AfterEach
	void tearDown() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void defaultStatusIsScheduled() {
		Appointment appointment = new Appointment();
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
	}

	@Test
	void cancelledAtIsNullByDefault() {
		Appointment appointment = new Appointment();
		assertThat(appointment.getCancelledAt()).isNull();
	}

	@Test
	void shouldSetAndGetAppointmentDate() {
		Appointment appointment = new Appointment();
		LocalDate date = LocalDate.of(2026, 4, 6);
		appointment.setAppointmentDate(date);
		assertThat(appointment.getAppointmentDate()).isEqualTo(date);
	}

	@Test
	void shouldSetAndGetStartAndEndTime() {
		Appointment appointment = new Appointment();
		appointment.setStartTime(LocalTime.of(9, 0));
		appointment.setEndTime(LocalTime.of(9, 30));
		assertThat(appointment.getStartTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(appointment.getEndTime()).isEqualTo(LocalTime.of(9, 30));
	}

	@Test
	void shouldFailValidationWhenAppointmentDateIsNull() {
		Appointment appointment = buildValidAppointment();
		appointment.setAppointmentDate(null);
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appointmentDate"));
	}

	@Test
	void shouldFailValidationWhenStartTimeIsNull() {
		Appointment appointment = buildValidAppointment();
		appointment.setStartTime(null);
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startTime"));
	}

	@Test
	void shouldFailValidationWhenEndTimeIsNull() {
		Appointment appointment = buildValidAppointment();
		appointment.setEndTime(null);
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("endTime"));
	}

	@Test
	void shouldFailValidationWhenPetIsNull() {
		Appointment appointment = buildValidAppointment();
		appointment.setPet(null);
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("pet"));
	}

	@Test
	void shouldFailValidationWhenVetIsNull() {
		Appointment appointment = buildValidAppointment();
		appointment.setVet(null);
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("vet"));
	}

	@Test
	void shouldFailValidationWhenAppointmentTypeIsNull() {
		Appointment appointment = buildValidAppointment();
		appointment.setAppointmentType(null);
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appointmentType"));
	}

	@Test
	void shouldPassValidationWithNotes500Characters() {
		Appointment appointment = buildValidAppointment();
		appointment.setNotes("A".repeat(500));
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		long notesViolations = violations.stream().filter(v -> v.getPropertyPath().toString().equals("notes")).count();
		assertThat(notesViolations).isZero();
	}

	@Test
	void shouldFailValidationWithNotes501Characters() {
		Appointment appointment = buildValidAppointment();
		appointment.setNotes("A".repeat(501));
		Set<ConstraintViolation<Appointment>> violations = validator.validate(appointment);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("notes"));
	}

	private Appointment buildValidAppointment() {
		Appointment appointment = new Appointment();
		appointment.setAppointmentDate(LocalDate.of(2026, 4, 6));
		appointment.setStartTime(LocalTime.of(9, 0));
		appointment.setEndTime(LocalTime.of(9, 30));
		// status defaults to SCHEDULED via field initializer; no setStatus call needed

		Pet pet = new Pet();
		pet.setName("Leo");
		appointment.setPet(pet);

		Vet vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");
		appointment.setVet(vet);

		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(30);
		appointment.setAppointmentType(type);

		// createdAt would be set by @PrePersist; set manually for validation
		appointment.setCreatedAt(java.time.LocalDateTime.now());

		return appointment;
	}

}
