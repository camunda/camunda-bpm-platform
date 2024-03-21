/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.spin.impl.test;

import java.lang.annotation.*;

/**
 * Annotation to define script variable bindings. Either used directly
 * in on the test method (if only one variable should be defined)
 *
 * <pre>
 *   {@literal @}Test
 *   {@literal @}Script
 *   {@literal @}ScriptVariable(name="input", value="test")
 *   public void shouldAccessInputVariable() {
 *     //...
 *   }
 * </pre>
 *
 * or in a list as the variables argument of the {@literal @}{@link Script} Annotation
 *
 * <pre>
 *   {@literal @}Test
 *   {@literal @}Script{
 *     variables = {
 *       &#064;ScriptVariable(name="input", value="test"),
 *       &#064;ScriptVariable(name="customerFile", file="customer.xml"),
 *     }
 *   }
 *   public void shouldAccessInputVariableAndCustomers() {
 *     //...
 *   }
 * </pre>
 *
 * @author Sebastian Menski
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScriptVariable {

  String name();

  String value() default "";

  String file() default "";

  boolean isNull() default false;

}
