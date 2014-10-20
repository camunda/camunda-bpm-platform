/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.impl.test;

import org.junit.rules.TestRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation to signal a script test for the {@link ScriptRule} {@link TestRule}.
 * The name of the script is optional and if omitted the package name is used as directory
 * and the ClassName.MethodName.ScriptExtension as filename. Additionally a list of
 * variables can be defined and the execute flag decides whether the script is execute
 * before the test function is called.
 *
 * The script variables can be accessed trough the {@link ScriptRule#variables}
 * field.
 *
 * Example usage:
 *
 * <pre>
 *   {@literal @}Test
 *   {@literal @}Script
 *   public void scriptNameWithoutExtension() {
 *     \\...
 *   }
 *
 *   {@literal @}Test
 *   {@literal @}Script("scriptNameWithoutExtension")
 *   public void notTheScriptName() {
 *     \\...
 *   }
 *
 *   {@literal @}Test
 *   {@literal @}Script(
 *     name = "scriptNameWithoutExtension",
 *     variables = {
 *       {@literal @}ScriptVariable(name="a", value="b"),
 *       {@literal @}ScriptVariable(name="f", file="test.xml")
 *     }
 *   )
 *   public void notTheScriptName() {
 *     \\...
 *   }
 *
 *   {@literal @}Test
 *   {@literal @}Script(
 *     name = "scriptNameWithoutExtension",
 *     execute = false
 *   )
 *   public void notTheScriptName() {
 *       Map<String, Object> variables = Collections.singletonMap("a", "b");
 *       script.execute(variables)
 *       assertEquals("b", script.variables.get("a"))
 *   }
 * </pre>
 *
 *
 * @author Sebastian Menski
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Script {

  String value() default "";

  String name() default "";

  ScriptVariable[] variables() default {};

  boolean execute() default true;

}
