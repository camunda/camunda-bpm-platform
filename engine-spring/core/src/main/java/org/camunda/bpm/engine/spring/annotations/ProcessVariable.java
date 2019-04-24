/*
 * Copyright 2011 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation used on method signatures to tell the runtime to
 * inject the current process' process variables as a {@link java.util.Map<String,Object>}.
 *
 * @author Josh Long
 * @since 1.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProcessVariable {
	/**
	 * Expression for matching against nested properties of the payload.
	 */
	String value() default "";
}
