package org.camunda.bpm.client.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.ExternalTaskClientImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PropertyPlaceholderConfiguration.class })
public class PropertyPlaceholderContextTest {

  @Autowired
  private ExternalTaskClient externalTaskClient;

  @Autowired
  private List<SubscribedExternalTask> scheduledExternalTasks;

  @Value("${client.baseUrl}")
  private String expectedBaseUrl;

  @Test
  public void startup() {
    assertThat(expectedBaseUrl).isNotBlank();
    assertThat(externalTaskClient).isNotNull();
    assertThat(scheduledExternalTasks).hasSize(2);
    scheduledExternalTasks.stream().flatMap(task -> task.getSubscriptions().stream()).forEach(subscription -> assertThat(subscription.isOpen()));
    String resolvedBaseUrl = ((ExternalTaskClientImpl) externalTaskClient).getTopicSubscriptionManager().getEngineClient().getBaseUrl();
    assertThat(resolvedBaseUrl).isEqualTo(expectedBaseUrl);
  }
}
