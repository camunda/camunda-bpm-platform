package org.camunda.bpm.spring.boot.starter.configuration.id;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.DbIdGenerator;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.spring.boot.starter.configuration.id.IdGeneratorConfiguration.SIMPLE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestApplication.class }, properties = "camunda.bpm.id-generator=" + SIMPLE)
public class SimpleUuidGeneratorIT {

  @Autowired
  private ProcessEngine processEngine;

  @Test
  public void configured_idGenerator_is_uuid() throws Exception {
    IdGenerator idGenerator = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getIdGenerator();

    assertThat(idGenerator).isInstanceOf(DbIdGenerator.class);
  }

}
