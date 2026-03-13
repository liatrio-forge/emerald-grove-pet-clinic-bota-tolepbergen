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

import java.time.DayOfWeek;
import java.time.LocalTime;
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
 * Unit tests for {@link ClinicScheduleConfig} entity validation and convenience methods.
 */
class ClinicScheduleConfigTests {

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
	void shouldSetAndGetDayOfWeek() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setDayOfWeek(1);
		assertThat(config.getDayOfWeek()).isEqualTo(1);
	}

	@Test
	void shouldSetAndGetOpenTime() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setOpenTime(LocalTime.of(9, 0));
		assertThat(config.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
	}

	@Test
	void shouldSetAndGetCloseTime() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setCloseTime(LocalTime.of(17, 0));
		assertThat(config.getCloseTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	void getDayOfWeekEnumReturnsMondayForValue1() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setDayOfWeek(1);
		assertThat(config.getDayOfWeekEnum()).isEqualTo(DayOfWeek.MONDAY);
	}

	@Test
	void getDayOfWeekEnumReturnsSundayForValue7() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setDayOfWeek(7);
		assertThat(config.getDayOfWeekEnum()).isEqualTo(DayOfWeek.SUNDAY);
	}

	@Test
	void getDayOfWeekEnumReturnsWednesdayForValue3() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setDayOfWeek(3);
		assertThat(config.getDayOfWeekEnum()).isEqualTo(DayOfWeek.WEDNESDAY);
	}

	@Test
	void shouldFailValidationWhenDayOfWeekIsNull() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setDayOfWeek(null);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations).anyMatch(
				v -> v.getPropertyPath().toString().equals("dayOfWeek") && v.getMessage().contains("must not be null"));
	}

	@Test
	void shouldFailValidationWhenOpenTimeIsNull() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setOpenTime(null);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations)
			.anyMatch(v -> v.getPropertyPath().toString().equals("openTime") && v.getMessage().contains("null"));
	}

	@Test
	void shouldFailValidationWhenCloseTimeIsNull() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setCloseTime(null);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations)
			.anyMatch(v -> v.getPropertyPath().toString().equals("closeTime") && v.getMessage().contains("null"));
	}

	@Test
	void shouldFailValidationWhenSlotDurationMinutesIsNull() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setSlotDurationMinutes(null);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("slotDurationMinutes"));
	}

	@Test
	void shouldFailValidationWhenSlotDurationMinutesIsLessThan5() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setSlotDurationMinutes(4);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("slotDurationMinutes")
				&& v.getMessage().contains("greater than or equal to 5"));
	}

	@Test
	void shouldPassValidationWhenSlotDurationMinutesIs5() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setSlotDurationMinutes(5);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		long slotViolations = violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals("slotDurationMinutes"))
			.count();
		assertThat(slotViolations).isZero();
	}

	@Test
	void shouldFailValidationWhenIsOpenIsNull() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setIsOpen(null);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("isOpen"));
	}

	@Test
	void shouldPassValidationForClosedDayWithSameTimes() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setDayOfWeek(7);
		config.setOpenTime(LocalTime.of(9, 0));
		config.setCloseTime(LocalTime.of(9, 0));
		config.setSlotDurationMinutes(30);
		config.setIsOpen(false);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldFailValidationWhenDayOfWeekIsZero() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setDayOfWeek(0);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dayOfWeek"));
	}

	@Test
	void shouldFailValidationWhenDayOfWeekIsEight() {
		ClinicScheduleConfig config = buildValidConfig();
		config.setDayOfWeek(8);
		Set<ConstraintViolation<ClinicScheduleConfig>> violations = validator.validate(config);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dayOfWeek"));
	}

	private ClinicScheduleConfig buildValidConfig() {
		ClinicScheduleConfig config = new ClinicScheduleConfig();
		config.setDayOfWeek(1);
		config.setOpenTime(LocalTime.of(9, 0));
		config.setCloseTime(LocalTime.of(17, 0));
		config.setSlotDurationMinutes(30);
		config.setIsOpen(true);
		return config;
	}

}
