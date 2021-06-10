/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.arguments;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.ObjectValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.CustomConstraint;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectValidatorTest {
	static final CustomConstraint<Instant> past = new CustomConstraint<Instant>() {

		@Override
		public boolean test(Instant instant) {
			return instant.isBefore(Instant.now());
		}

		@Override
		public String defaultMessageFormat() {
			return "\"{0}\" must be past";
		}

		@Override
		public String messageKey() {
			return "instant.past";
		}
	};

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(ObjectValidator<Instant, Date> dataValidator) {
		final Validated<Date> dataValidated = dataValidator
				.validate(Instant.ofEpochMilli(1000L));
		assertThat(dataValidated.isValid()).isTrue();
		assertThat(dataValidated.value().getTime()).isEqualTo(1000L);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(ObjectValidator<Instant, Date> dataValidator) {
		final Validated<Date> dataValidated = dataValidator
				.validate(Instant.now().plusSeconds(1));
		assertThat(dataValidated.isValid()).isFalse();
		final ConstraintViolations violations = dataValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("createdAt");
		assertThat(violations.get(0).messageKey()).isEqualTo("instant.past");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(ObjectValidator<Instant, Date> dataValidator) {
		final Date date = dataValidator.validated(Instant.ofEpochMilli(1000L));
		assertThat(date.getTime()).isEqualTo(1000L);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(ObjectValidator<Instant, Date> dataValidator) {
		assertThatThrownBy(() -> dataValidator.validated(Instant.now().plusSeconds(1)))
				.isInstanceOf(ConstraintViolationsException.class)
				.hasMessageContaining("\"createdAt\" must be past");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(ObjectValidator<Instant, Date> dataValidator) {
		final Map<String, Instant> params = Collections.singletonMap("createdAt",
				Instant.ofEpochMilli(1000L));
		final Arguments1Validator<Map<String, Instant>, Date> mapValidator = dataValidator
				.compose(map -> map.get("createdAt"));
		final Validated<Date> dataValidated = mapValidator.validate(params);
		assertThat(dataValidated.isValid()).isTrue();
		assertThat(dataValidated.value().getTime()).isEqualTo(1000L);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(ObjectValidator<Instant, Date> dataValidator) {
		final Map<String, Instant> params = Collections.singletonMap("createdAt",
				Instant.now().plusSeconds(1));
		final Arguments1Validator<Map<String, Instant>, Date> mapValidator = dataValidator
				.compose(map -> map.get("createdAt"));
		final Validated<Date> dataValidated = mapValidator.validate(params);
		assertThat(dataValidated.isValid()).isFalse();
		final ConstraintViolations violations = dataValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("createdAt");
		assertThat(violations.get(0).messageKey()).isEqualTo("instant.past");
	}

	static Stream<ObjectValidator<Instant, Date>> validators() {
		return Stream.of(
				ObjectValidatorBuilder
						.<Instant> of("createdAt",
								c -> c.notNull().predicateNullable(past))
						.build(Date::from),
				ObjectValidatorBuilder
						.<Instant> of("createdAt",
								c -> c.notNull().predicateNullable(past))
						.build().andThen(Date::from));
	}
}