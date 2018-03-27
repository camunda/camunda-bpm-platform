package org.camunda.bpm.spring.boot.starter.configuration.id;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.spring.boot.starter.configuration.id.IdGeneratorConfiguration.PREFIXED;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {TestApplication.class},
  properties = {
    "camunda.bpm.id-generator=" + PREFIXED,
    "spring.application.name=myapp"
  })
public class PrefixedUuidGeneratorIT {

  @Autowired
  private IdGenerator idGenerator;

  @Autowired
  private CamundaBpmProperties properties;

  @Autowired
  private ProcessEngine processEngine;

  @Test
  public void property_is_set() throws Exception {
    assertThat(properties.getIdGenerator()).isEqualTo(IdGeneratorConfiguration.PREFIXED);
  }

  @Test
  public void configured_idGenerator_is_uuid() throws Exception {
    final IdGenerator idGenerator = CamundaSpringBootUtil.get(processEngine).getIdGenerator();

    assertThat(idGenerator).isOfAnyClassIn(PrefixedUuidGenerator.class);
  }

  @Test
  public void nextId_is_uuid() throws Exception {
    assertThat(idGenerator.getNextId().split("-")).hasSize(6).startsWith("myapp");
  }
}
