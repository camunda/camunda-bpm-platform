package org.camunda.bpm.engine.spring.test.plugin;

import org.camunda.bpm.engine.spring.SpringProcessEnginePlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringProcessEnginePluginTest.TestConfig.class)
public class SpringProcessEnginePluginTest {

  public static class TestConfig {

    @Bean
    public SpringProcessEnginePlugin theBeanName() {
      return new SpringProcessEnginePlugin(){};
    }
  }

  @Autowired
  private SpringProcessEnginePlugin plugin;

  @Test
  public void verifyToString() throws Exception {
    Assert.assertEquals(plugin.toString(), "theBeanName");
  }
}