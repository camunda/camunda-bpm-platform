package org.camunda.bpm.spring.boot.starter.property;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"camunda.bpm.metrics.enabled=false", "camunda.bpm.metrics.db-reporter-activate=false"})
public class MetricsPropertiesTest extends ParsePropertiesHelper {

  @Test
  public void verifyCorrectProperties() throws Exception {
    assertThat(metrics.isEnabled()).isFalse();
    assertThat(metrics.isDbReporterActivate()).isFalse();
  }
}
