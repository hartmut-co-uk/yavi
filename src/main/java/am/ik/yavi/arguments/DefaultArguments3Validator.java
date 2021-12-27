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

import java.util.Locale;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function3;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class DefaultArguments3Validator<A1, A2, A3, X>
		implements Arguments3Validator<A1, A2, A3, X> {
	protected final Validator<Arguments3<A1, A2, A3>> validator;
	protected final Function3<? super A1, ? super A2, ? super A3, ? extends X> mapper;

	public DefaultArguments3Validator(Validator<Arguments3<A1, A2, A3>> validator,
			Function3<? super A1, ? super A2, ? super A3, ? extends X> mapper) {
		this.validator = validator;
		this.mapper = mapper;
	}

	/**
	 * @since 0.10.0
	 */
	@Override
	public DefaultArguments3Validator<A1, A2, A3, Supplier<X>> lazy() {
		return new DefaultArguments3Validator<>(this.validator,
				(a1, a2, a3) -> () -> this.mapper.apply(a1, a2, a3));
	}

	@Override
	public Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3,
			Locale locale, ConstraintGroup constraintGroup) {
		return this.validator.applicative()
				.validate(Arguments.of(a1, a2, a3), locale, constraintGroup)
				.map(values -> values.map(this.mapper));
	}
}
