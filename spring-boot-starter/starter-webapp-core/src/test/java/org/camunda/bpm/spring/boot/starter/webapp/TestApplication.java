package org.camunda.bpm.spring.boot.starter.webapp;

import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CamundaBpmProperties.class)
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

}
