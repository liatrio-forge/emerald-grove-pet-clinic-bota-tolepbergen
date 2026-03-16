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
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Unit tests for {@link VetTimeOff} entity validation.
 */
class VetTimeOffTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.afterPropertiesSet();
		this.validator = localValidatorFactoryBean;
	}

	@Test
	void shouldSetAndGetVet() {
		Vet vet = new Vet();
		vet.setFirstName("Helen");
		vet.setLastName("Leary");
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setVet(vet);
		assertThat(timeOff.getVet()).isEqualTo(vet);
	}

	@Test
	void shouldSetAndGetDate() {
		VetTimeOff timeOff = new VetTimeOff();
		LocalDate date = LocalDate.of(2026, 4, 6);
		timeOff.setDate(date);
		assertThat(timeOff.getDate()).isEqualTo(LocalDate.of(2026, 4, 6));
	}

	@Test
	void shouldSetAndGetReason() {
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setReason("Conference");
		assertThat(timeOff.getReason()).isEqualTo("Conference");
	}

	@Test
	void shouldAllowNullReason() {
		VetTimeOff timeOff = buildValidTimeOff();
		timeOff.setReason(null);
		Set<ConstraintViolation<VetTimeOff>> violations = validator.validate(timeOff);
		long reasonViolations = violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals("reason"))
			.count();
		assertThat(reasonViolations).isZero();
	}

	@Test
	void shouldFailValidationWhenVetIsNull() {
		VetTimeOff timeOff = buildValidTimeOff();
		timeOff.setVet(null);
		Set<ConstraintViolation<VetTimeOff>> violations = validator.validate(timeOff);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("vet"));
	}

	@Test
	void shouldFailValidationWhenDateIsNull() {
		VetTimeOff timeOff = buildValidTimeOff();
		timeOff.setDate(null);
		Set<ConstraintViolation<VetTimeOff>> violations = validator.validate(timeOff);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("date"));
	}

	@Test
	void shouldFailValidationWhenReasonExceeds255Characters() {
		VetTimeOff timeOff = buildValidTimeOff();
		timeOff.setReason("A".repeat(256));
		Set<ConstraintViolation<VetTimeOff>> violations = validator.validate(timeOff);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("reason")
				&& v.getMessage().contains("size must be between 0 and 255"));
	}

	@Test
	void shouldPassValidationWithReason255Characters() {
		VetTimeOff timeOff = buildValidTimeOff();
		timeOff.setReason("A".repeat(255));
		Set<ConstraintViolation<VetTimeOff>> violations = validator.validate(timeOff);
		long reasonViolations = violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals("reason"))
			.count();
		assertThat(reasonViolations).isZero();
	}

	private VetTimeOff buildValidTimeOff() {
		Vet vet = new Vet();
		vet.setFirstName("Helen");
		vet.setLastName("Leary");
		VetTimeOff timeOff = new VetTimeOff();
		timeOff.setVet(vet);
		timeOff.setDate(LocalDate.of(2026, 4, 6));
		timeOff.setReason("Conference");
		return timeOff;
	}

}
