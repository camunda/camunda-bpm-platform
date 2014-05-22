package org.camunda.spin.test;

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
