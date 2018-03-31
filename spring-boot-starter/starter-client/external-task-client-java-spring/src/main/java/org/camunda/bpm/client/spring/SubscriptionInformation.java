package org.camunda.bpm.client.spring;

import lombok.Data;

@Data
public class SubscriptionInformation {

  public static final long DEFAULT_LOCK_DURATION = 1000;
  public static final boolean DEFAULT_AUTO_SUBSCRIBE = true;
  public static final boolean DEFAULT_AUTO_OPEN = true;

  private final String topicName;
  private long lockDuration = DEFAULT_LOCK_DURATION;
  private boolean autoSubscribe = DEFAULT_AUTO_SUBSCRIBE;
  private boolean autoOpen = DEFAULT_AUTO_OPEN;
  private String[] externalTaskClientIds = new String[] {};

}
