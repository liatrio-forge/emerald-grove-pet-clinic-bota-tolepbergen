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

/**
 * Immutable value object representing a single scheduling conflict.
 *
 * <p>
 * The {@code conflictingAppointment} field is nullable -- it is only set for overlap
 * conflicts ({@link ConflictType#VET_OVERLAP}, {@link ConflictType#PET_OVERLAP},
 * {@link ConflictType#OWNER_OVERLAP}).
 */
public record SchedulingConflict(ConflictType type, String message, Appointment conflictingAppointment) {

}
