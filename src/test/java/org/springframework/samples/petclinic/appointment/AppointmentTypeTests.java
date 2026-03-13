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

import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Unit tests for {@link AppointmentType} entity validation.
 */
class AppointmentTypeTests {

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
	void shouldSetAndGetName() {
		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		assertThat(type.getName()).isEqualTo("Checkup");
	}

	@Test
	void shouldSetAndGetDefaultDurationMinutes() {
		AppointmentType type = new AppointmentType();
		type.setDefaultDurationMinutes(30);
		assertThat(type.getDefaultDurationMinutes()).isEqualTo(30);
	}

	@Test
	void shouldSetAndGetDescription() {
		AppointmentType type = new AppointmentType();
		type.setDescription("A general health checkup");
		assertThat(type.getDescription()).isEqualTo("A general health checkup");
	}

	@Test
	void shouldAllowNullDescription() {
		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(30);
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		long descriptionViolations = violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals("description"))
			.count();
		assertThat(descriptionViolations).isZero();
	}

	@Test
	void shouldAllowNullRequiredSpecialty() {
		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(30);
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		long specialtyViolations = violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals("requiredSpecialty"))
			.count();
		assertThat(specialtyViolations).isZero();
	}

	@Test
	void shouldFailValidationWhenNameIsBlank() {
		AppointmentType type = new AppointmentType();
		type.setName("");
		type.setDefaultDurationMinutes(30);
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
	}

	@Test
	void shouldFailValidationWhenDefaultDurationMinutesIsNull() {
		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(null);
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("defaultDurationMinutes"));
	}

	@Test
	void shouldFailValidationWhenDefaultDurationMinutesIsZero() {
		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(0);
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("defaultDurationMinutes")
				&& v.getMessage().contains("greater than or equal to 1"));
	}

	@Test
	void shouldPassValidationWhenDefaultDurationMinutesIsOne() {
		AppointmentType type = new AppointmentType();
		type.setName("QuickCheck");
		type.setDefaultDurationMinutes(1);
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		long durationViolations = violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals("defaultDurationMinutes"))
			.count();
		assertThat(durationViolations).isZero();
	}

	@Test
	void shouldPassValidationWhenDescriptionIs255Characters() {
		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(30);
		type.setDescription("A".repeat(255));
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		long descriptionViolations = violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals("description"))
			.count();
		assertThat(descriptionViolations).isZero();
	}

	@Test
	void shouldFailValidationWhenDescriptionIs256Characters() {
		AppointmentType type = new AppointmentType();
		type.setName("Checkup");
		type.setDefaultDurationMinutes(30);
		type.setDescription("A".repeat(256));
		Set<ConstraintViolation<AppointmentType>> violations = validator.validate(type);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
	}

}
