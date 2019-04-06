package org.camunda.bpm.client.spring;

import static org.camunda.bpm.client.spring.helper.AnnotationNullValueHelper.NULL_VALUE_LONG;
import static org.camunda.bpm.client.spring.helper.AnnotationNullValueHelper.NULL_VALUE_STRING;

import java.lang.annotation.*;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TaskSubscriptionConfiguration.class)
public @interface EnableTaskSubscription {

  String baseUrl() default "";

  String id() default "";

  int maxTasks() default 10;

  String workerId() default NULL_VALUE_STRING;

  long asyncResponseTimeout() default NULL_VALUE_LONG;

  boolean autoFetchingEnabled() default true;

  long lockDuration() default 20_000;

  String dateFormat() default NULL_VALUE_STRING;

  String defaultSerializationFormat() default NULL_VALUE_STRING;

}
