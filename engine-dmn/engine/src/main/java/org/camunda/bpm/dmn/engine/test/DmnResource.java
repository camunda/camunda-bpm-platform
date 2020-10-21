package org.camunda.bpm.dmn.engine.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for a test method or class to create and delete a deployment around a test method.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DmnResource {

  /** Specify classpath resources that make up the dmn definition. */
  String[] resources() default {};
}
