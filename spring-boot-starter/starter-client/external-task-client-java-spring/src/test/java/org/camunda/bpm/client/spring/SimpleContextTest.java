package org.camunda.bpm.client.spring;

import org.camunda.bpm.client.ExternalTaskClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SimpleConfiguration.class})
public class SimpleContextTest {

    @Autowired
    private ExternalTaskClient externalTaskClient;

    @Autowired
    private List<SubscribedExternalTask> scheduledExternalTasks;

    @Test
    public void startup() {
        assertThat(externalTaskClient).isNotNull();
        assertThat(scheduledExternalTasks).hasSize(2);
        scheduledExternalTasks.stream().flatMap(task -> task.getSubscriptions().stream())
                .forEach(subscription -> assertThat(subscription.isOpen()));
    }
}
