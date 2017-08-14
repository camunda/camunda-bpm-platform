package org.camunda.bpm.spring.boot.starter.property;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class CamundaBpmPropertiesTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void initResourcePatterns() {
    final String[] patterns = CamundaBpmProperties.initDeploymentResourcePattern();

    assertThat(patterns).hasSize(7);
    assertThat(patterns).containsOnly("classpath*:**/*.bpmn", "classpath*:**/*.bpmn20.xml", "classpath*:**/*.dmn", "classpath*:**/*.dmn11.xml",
      "classpath*:**/*.cmmn", "classpath*:**/*.cmmn10.xml", "classpath*:**/*.cmmn11.xml");
  }

  @Test
  public void application_defaults() throws Exception {
    assertThat(new CamundaBpmProperties().getApplication().isDeleteUponUndeploy()).isFalse();
  }

  @Test
  public void restrict_allowed_values_for_dbUpdate() {
    new CamundaBpmProperties().getDatabase().setSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    new CamundaBpmProperties().getDatabase().setSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
    new CamundaBpmProperties().getDatabase().setSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE);
    new CamundaBpmProperties().getDatabase().setSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
    new CamundaBpmProperties().getDatabase().setSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("foo");

    new CamundaBpmProperties().getDatabase().setSchemaUpdate("foo");
  }


}
