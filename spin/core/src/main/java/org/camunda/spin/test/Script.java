package org.camunda.spin.test;

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
