package org.camunda.bpm.client.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ExtendedConfiguration.class })
public class ExtendedContextTest {

  @Autowired
  @Qualifier("First")
  private ExternalTaskClient externalTaskClientFirst;

  @Autowired
  @Qualifier("Second")
  private ExternalTaskClient externalTaskClientSecond;

  @Autowired
  private List<SubscribedExternalTask> scheduledExternalTasks;

  @Test
  public void startup() {
    assertThat(externalTaskClientFirst).isNotNull();
    assertThat(externalTaskClientSecond).isNotNull();
    assertThat(scheduledExternalTasks).hasSize(2);
    scheduledExternalTasks.stream().flatMap(task -> task.getSubscriptions().stream()).forEach(subscription -> assertThat(subscription.isOpen()));
  }

  @Test
  public void testSubscription() {
    testSubscription(externalTaskClientFirst, "methodSubscription");
    testSubscription(externalTaskClientSecond, "testClassSubscription");
  }

  private void testSubscription(ExternalTaskClient taskClient, String topicName) {
    ExternalTaskClientImpl clientImpl = (ExternalTaskClientImpl) taskClient;
    List<TopicSubscription> subscriptions = clientImpl.getTopicSubscriptionManager().getSubscriptions();
    assertThat(subscriptions).hasSize(1);
    TopicSubscription subscription = subscriptions.iterator().next();
    assertThat(subscription.getTopicName()).isEqualTo(topicName);
  }
}
