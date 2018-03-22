package org.camunda.bpm.spring.boot.starter.actuator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

@RunWith(MockitoJUnitRunner.class)
public class ProcessEngineHealthIndicatorTest {

  private static final String PROCESS_ENGINE_NAME = "process engine name";

  @Mock
  private ProcessEngine processEngine;

  @Test(expected = IllegalArgumentException.class)
  public void nullTest() {
    new ProcessEngineHealthIndicator(null);
  }

  @Test
  public void upTest() {
    when(processEngine.getName()).thenReturn(PROCESS_ENGINE_NAME);
    Health health = new ProcessEngineHealthIndicator(processEngine).health();
    assertEquals(Status.UP, health.getStatus());
    assertEquals(PROCESS_ENGINE_NAME, health.getDetails().get("name"));
  }
}
