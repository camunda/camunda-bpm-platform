package org.camunda.bpm.spring.boot.starter;

import javax.servlet.ServletContext;

import org.camunda.bpm.spring.boot.starter.telemetry.CamundaApplicationServerConfigurator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaBpmTelemetryConfiguration {

  @Configuration
  @ConditionalOnClass(ServletContext.class)
  static class TelemetryApplicationServerConfiguration {

    @Bean
    public static CamundaApplicationServerConfigurator applicationServerConfigurator() {
      return new CamundaApplicationServerConfigurator();
    }
  }
}
