package org.camunda.bpm.client.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TaskSubscriptionConfiguration.class)
public @interface EnableTaskSubscription {

  String baseUrl() default "";

  String id() default "";
}
