package org.camunda.bpm.spring.boot.starter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@TestConfiguration
public class AdditionalCammundaBpmConfigurations {

  @Bean
  public ProcessEnginePlugin beforeStandardConfiguration() {
    return new BeforeStandardConfiguration();
  }

  @Bean
  public ProcessEnginePlugin afterStandardConfiguration() {
    return new AfterStandardConfiguration();
  }

  @Order(Ordering.DEFAULT_ORDER - 1)
  public static class BeforeStandardConfiguration extends AbstractProcessEnginePlugin {

    static boolean PROCESSED = false;

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
      assertNull(configuration.getDataSource());
      PROCESSED = true;
    }
  }

  @Order(Ordering.DEFAULT_ORDER + 1)
  public static class AfterStandardConfiguration extends  AbstractProcessEnginePlugin {

    static boolean PROCESSED = false;

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
      assertNotNull(configuration.getDataSource());
      PROCESSED = true;
    }
  }
}
