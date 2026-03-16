package org.springframework.samples.petclinic.owner;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class VisitValidator implements Validator {

	private final Clock clock;

	public VisitValidator() {
		this(Clock.systemDefaultZone());
	}

	public VisitValidator(Clock clock) {
		this.clock = clock;
	}

	@Override
	public void validate(Object obj, Errors errors) {
		Visit visit = (Visit) obj;
		LocalDate date = visit.getDate();

		if (date == null) {
			errors.rejectValue("date", "required", "is required");
			return;
		}

		if (date.isBefore(LocalDate.now(clock))) {
			errors.rejectValue("date", "pastDate", "must not be in the past");
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Visit.class.isAssignableFrom(clazz);
	}

}
