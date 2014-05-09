package org.camunda.spin.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a {@link ScriptEngine} of a test class
 * using the {@link ScriptEngineRule}.
 *
 * <pre>
 *   {@literal @}ScriptEngine("python")
 *   public class SpinXmlPythonTest extends SpinXmlScriptTest {
 *     // ...
 *   }
 * </pre>
 *
 * @author Sebastian Menski
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptEngine {

  String value();

}
