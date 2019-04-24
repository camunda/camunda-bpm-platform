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
 * indicates that a method is to be enlisted as a handler for a given BPMN state
 *
 * @author Josh Long
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface State {

	/**
	 * the business process name
	 */
	String process () default "";

	/**
	 * the state that the component responds to,
	 */
	String state () default "";

    /**
     * by default, this will be the #stateName
     */
	String value() default "";
}
