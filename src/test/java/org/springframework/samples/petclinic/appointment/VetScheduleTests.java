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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Unit tests for {@link VetSchedule} entity validation and convenience methods.
 */
class VetScheduleTests {

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
	void getDayOfWeekEnumReturnsWednesdayForValue3() {
		VetSchedule schedule = new VetSchedule();
		schedule.setDayOfWeek(3);
		assertThat(schedule.getDayOfWeekEnum()).isEqualTo(DayOfWeek.WEDNESDAY);
	}

	@Test
	void getDayOfWeekEnumReturnsMondayForValue1() {
		VetSchedule schedule = new VetSchedule();
		schedule.setDayOfWeek(1);
		assertThat(schedule.getDayOfWeekEnum()).isEqualTo(DayOfWeek.MONDAY);
	}

	@Test
	void getDayOfWeekEnumThrowsForValue0() {
		VetSchedule schedule = new VetSchedule();
		schedule.setDayOfWeek(0);
		assertThatThrownBy(schedule::getDayOfWeekEnum).isInstanceOf(java.time.DateTimeException.class);
	}

	@Test
	void getDayOfWeekEnumThrowsForValue8() {
		VetSchedule schedule = new VetSchedule();
		schedule.setDayOfWeek(8);
		assertThatThrownBy(schedule::getDayOfWeekEnum).isInstanceOf(java.time.DateTimeException.class);
	}

	@Test
	void shouldSetAndGetStartTime() {
		VetSchedule schedule = new VetSchedule();
		schedule.setStartTime(LocalTime.of(9, 0));
		assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
	}

	@Test
	void shouldSetAndGetEndTime() {
		VetSchedule schedule = new VetSchedule();
		schedule.setEndTime(LocalTime.of(17, 0));
		assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(17, 0));
	}

	@Test
	void shouldFailValidationWhenVetIsNull() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setVet(null);
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("vet"));
	}

	@Test
	void shouldFailValidationWhenDayOfWeekIsNull() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setDayOfWeek(null);
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dayOfWeek"));
	}

	@Test
	void shouldFailValidationWhenStartTimeIsNull() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setStartTime(null);
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startTime"));
	}

	@Test
	void shouldFailValidationWhenEndTimeIsNull() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setEndTime(null);
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("endTime"));
	}

	@Test
	void shouldFailValidationWhenIsAvailableIsNull() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setIsAvailable(null);
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("isAvailable"));
	}

	@Test
	void shouldFailValidationWhenDayOfWeekIsBelowMin() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setDayOfWeek(0);
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dayOfWeek"));
	}

	@Test
	void shouldFailValidationWhenDayOfWeekIsAboveMax() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setDayOfWeek(8);
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dayOfWeek"));
	}

	@Test
	void shouldFailValidationWhenStartTimeEqualsEndTime() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setStartTime(LocalTime.of(9, 0));
		schedule.setEndTime(LocalTime.of(9, 0));
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startBeforeEnd"));
	}

	@Test
	void shouldFailValidationWhenStartTimeAfterEndTime() {
		VetSchedule schedule = buildValidSchedule();
		schedule.setStartTime(LocalTime.of(17, 0));
		schedule.setEndTime(LocalTime.of(9, 0));
		Set<ConstraintViolation<VetSchedule>> violations = validator.validate(schedule);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("startBeforeEnd"));
	}

	private VetSchedule buildValidSchedule() {
		VetSchedule schedule = new VetSchedule();
		Vet vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");
		schedule.setVet(vet);
		schedule.setDayOfWeek(1);
		schedule.setStartTime(LocalTime.of(9, 0));
		schedule.setEndTime(LocalTime.of(17, 0));
		schedule.setIsAvailable(true);
		return schedule;
	}

}
