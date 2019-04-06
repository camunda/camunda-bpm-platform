package org.camunda.bpm.client.spring.boot.starter;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.SubscribedExternalTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CamundaBpmClientAutoConfigurationTest {

    @Autowired
    private ExternalTaskClient externalTaskClient;

    @Autowired
    private List<SubscribedExternalTask> scheduledExternalTasks;

    @Test
    public void startup() {
        assertThat(externalTaskClient).isNotNull();
        assertThat(scheduledExternalTasks).isNotEmpty();
    }
}
