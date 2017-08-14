package org.camunda.bpm.spring.boot.starter.property;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParsePropertiesHelper.TestConfig.class)
public abstract class ParsePropertiesHelper {

  @EnableConfigurationProperties(CamundaBpmProperties.class)
  public static class TestConfig {
  }

  @Autowired
  protected CamundaBpmProperties properties;

  protected MetricsProperty metrics;
  protected ApplicationProperty application;

  @PostConstruct
  public void init() {
    metrics = properties.getMetrics();
    application = properties.getApplication();
  }
}
