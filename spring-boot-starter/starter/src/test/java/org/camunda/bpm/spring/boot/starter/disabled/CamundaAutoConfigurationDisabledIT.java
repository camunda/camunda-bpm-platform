package org.camunda.bpm.spring.boot.starter.disabled;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {CamundaAutoConfigurationDisabledIT.NoCamundaApplication.class},
  webEnvironment = WebEnvironment.NONE,
  properties = {"camunda.bpm.enabled=false"}
)
public class CamundaAutoConfigurationDisabledIT {

  @SpringBootApplication
  public static class NoCamundaApplication {

  }

  @Autowired
  private Optional<ProcessEngine> processEngine;

  @Test
  public void processEngineNotConfigured() {
    assertThat(processEngine.isPresent()).isFalse();
  }

}
