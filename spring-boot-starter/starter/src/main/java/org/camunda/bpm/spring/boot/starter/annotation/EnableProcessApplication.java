package org.camunda.bpm.spring.boot.starter.annotation;

import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SpringBootProcessApplication.class)
@Documented
@Inherited
public @interface EnableProcessApplication {

  String value() default "";
}
