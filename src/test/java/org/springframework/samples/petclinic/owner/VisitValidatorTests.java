package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@DisabledInNativeImage
class VisitValidatorTests {

	private final VisitValidator validator = new VisitValidator();

	@Test
	void shouldAcceptVisitWithTodayDate() {
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		visit.setDescription("Annual checkup");

		Errors errors = new BeanPropertyBindingResult(visit, "visit");
		validator.validate(visit, errors);

		assertThat(errors.getFieldErrors("date")).isEmpty();
	}

	@Test
	void shouldAcceptVisitWithFutureDate() {
		Visit visit = new Visit();
		visit.setDate(LocalDate.now().plusDays(7));
		visit.setDescription("Vaccination");

		Errors errors = new BeanPropertyBindingResult(visit, "visit");
		validator.validate(visit, errors);

		assertThat(errors.getFieldErrors("date")).isEmpty();
	}

	@Test
	void shouldRejectVisitWithPastDate() {
		Visit visit = new Visit();
		visit.setDate(LocalDate.now().minusDays(1));
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
