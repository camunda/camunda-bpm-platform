package org.camunda.bpm.client.spring;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static org.camunda.bpm.client.spring.SubscriptionInformation.*;
import static org.camunda.bpm.client.spring.helper.AnnotationNullValueHelper.NULL_VALUE_STRING;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD })
public @interface ExternalTaskSubscription {

  String topicName();

  long lockDuration() default DEFAULT_LOCK_DURATION;

  boolean autoSubscribe() default DEFAULT_AUTO_SUBSCRIBE;

  boolean autoOpen() default DEFAULT_AUTO_OPEN;

  String[] externalTaskClientIds() default {};

  String[] variableNames() default { NULL_VALUE_STRING };

  String businessKey() default NULL_VALUE_STRING;

}
