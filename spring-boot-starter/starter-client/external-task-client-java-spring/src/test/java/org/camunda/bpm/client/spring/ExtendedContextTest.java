package org.camunda.bpm.client.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.spring.interceptor.ClientIdAcceptingClientRequestInterceptor;
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

  @Autowired
  private ClientRequestInterceptor clientRequestInterceptor;

  @Autowired
  private ClientIdAcceptingClientRequestInterceptor idAcceptingInterceptor;

  @Test
  public void startup() {
    assertThat(externalTaskClientFirst).isNotNull();
    assertThat(externalTaskClientSecond).isNotNull();
    assertThat(scheduledExternalTasks).hasSize(2);
    scheduledExternalTasks.stream().flatMap(task -> task.getSubscriptions().stream()).forEach(subscription -> assertThat(subscription.isOpen()));
  }

  @Test
  public void testSubscriptionAndInterceptor() {
    testSubscriptionAndInterceptor(externalTaskClientFirst, "methodSubscription", clientRequestInterceptor);
    testSubscriptionAndInterceptor(externalTaskClientSecond, "testClassSubscription", clientRequestInterceptor, idAcceptingInterceptor);
  }

  private void testSubscriptionAndInterceptor(ExternalTaskClient taskClient, String topicName, ClientRequestInterceptor... expectedInterceptors) {
    ExternalTaskClientImpl clientImpl = (ExternalTaskClientImpl) taskClient;
    List<TopicSubscription> subscriptions = clientImpl.getTopicSubscriptionManager().getSubscriptions();
    assertThat(subscriptions).hasSize(1);
    TopicSubscription subscription = subscriptions.iterator().next();
    assertThat(subscription.getTopicName()).isEqualTo(topicName);
    List<ClientRequestInterceptor> interceptors = clientImpl.getRequestInterceptorHandler().getInterceptors();
    if (expectedInterceptors == null) {
      assertThat(interceptors).hasSize(0);
    } else {
      assertThat(interceptors).containsOnly(expectedInterceptors);
    }
  }
}
