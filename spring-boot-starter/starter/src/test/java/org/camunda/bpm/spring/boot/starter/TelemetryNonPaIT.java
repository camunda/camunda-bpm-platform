package org.camunda.bpm.spring.boot.starter;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {TestApplication.class},
  webEnvironment = WebEnvironment.RANDOM_PORT
)
public class TelemetryNonPaIT extends AbstractCamundaAutoConfigurationIT {

  @Test
  public void shouldSubmitApplicationServerData() {
    TelemetryRegistry telemetryRegistry = processEngine.getProcessEngineConfiguration().getTelemetryRegistry();

    // then
    ApplicationServer applicationServer = telemetryRegistry.getApplicationServer();
    assertThat(applicationServer).isNotNull();
    assertThat(applicationServer.getVendor()).isEqualTo("Apache Tomcat");
    assertThat(applicationServer.getVersion()).isNotNull();
  }
}
