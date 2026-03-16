package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@DisabledInNativeImage
class VisitValidatorTests {

	private static final LocalDate FIXED_DATE = LocalDate.of(2026, 3, 5);

	private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(),
			ZoneId.systemDefault());

	private final VisitValidator validator = new VisitValidator(FIXED_CLOCK);

	@Test
	void shouldAcceptVisitWithTodayDate() {
		Visit visit = new Visit();
		visit.setDate(FIXED_DATE);
		visit.setDescription("Annual checkup");

		Errors errors = new BeanPropertyBindingResult(visit, "visit");
		validator.validate(visit, errors);

		assertThat(errors.getFieldErrors("date")).isEmpty();
	}

	@Test
	void shouldAcceptVisitWithFutureDate() {
		Visit visit = new Visit();
		visit.setDate(FIXED_DATE.plusDays(7));
		visit.setDescription("Vaccination");

		Errors errors = new BeanPropertyBindingResult(visit, "visit");
		validator.validate(visit, errors);

		assertThat(errors.getFieldErrors("date")).isEmpty();
	}

	@Test
	void shouldRejectVisitWithPastDate() {
		Visit visit = new Visit();
		visit.setDate(FIXED_DATE.minusDays(1));
		visit.setDescription("Past visit");

		Errors errors = new BeanPropertyBindingResult(visit, "visit");
		validator.validate(visit, errors);

		assertThat(errors.getFieldErrors("date")).hasSize(1);
		assertThat(errors.getFieldError("date").getCode()).isEqualTo("pastDate");
	}

	@Test
	void shouldRejectVisitWithNullDate() {
		Visit visit = new Visit();
		visit.setDate(null);
		visit.setDescription("No date");

		Errors errors = new BeanPropertyBindingResult(visit, "visit");
		validator.validate(visit, errors);

		assertThat(errors.getFieldErrors("date")).hasSize(1);
		assertThat(errors.getFieldError("date").getCode()).isEqualTo("required");
	}

}
