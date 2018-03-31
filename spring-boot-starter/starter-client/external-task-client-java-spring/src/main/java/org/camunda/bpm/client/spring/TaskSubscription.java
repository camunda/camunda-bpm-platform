package org.camunda.bpm.client.spring;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, METHOD })
public @interface TaskSubscription {

  String topicName();

  long lockDuration() default SubscriptionInformation.DEFAULT_LOCK_DURATION;

  boolean autoSubscribe() default SubscriptionInformation.DEFAULT_AUTO_SUBSCRIBE;

  boolean autoOpen() default SubscriptionInformation.DEFAULT_AUTO_OPEN;

  String[] externalTaskClientIds() default {};

}
