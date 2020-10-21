package org.camunda.bpm.dmn.engine.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for {@link org.camunda.bpm.dmn.engine.DmnDecision} retrieval around a test method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DmnDecisionKey {

  /**
   * Specify dmn definition by key
   */
  String name() default "";
}
