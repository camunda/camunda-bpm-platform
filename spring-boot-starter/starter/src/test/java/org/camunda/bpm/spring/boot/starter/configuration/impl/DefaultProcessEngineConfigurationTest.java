package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

public class DefaultProcessEngineConfigurationTest {

  private final DefaultProcessEngineConfiguration instance = new DefaultProcessEngineConfiguration();
  private final SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
  private final CamundaBpmProperties properties = new CamundaBpmProperties();

  @Before
  public void setUp() throws Exception {
    ReflectionTestUtils.setField(instance, "camundaBpmProperties", properties);
    initIdGenerator(null);
  }

  @Test
  public void setName_if_not_empty() throws Exception {
    properties.setProcessEngineName("foo");
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo("foo");
  }

  @Test
  public void setName_ignore_empty() throws Exception {
    properties.setProcessEngineName(null);
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo(ProcessEngines.NAME_DEFAULT);

    properties.setProcessEngineName(" ");
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo(ProcessEngines.NAME_DEFAULT);
  }

  @Test
  public void setName_ignore_hyphen() throws Exception {
    properties.setProcessEngineName("foo-bar");
    instance.preInit(configuration);
    assertThat(configuration.getProcessEngineName()).isEqualTo(ProcessEngines.NAME_DEFAULT);
  }

  @Test
  public void setDefaultSerializationFormat() {
    final String defaultSerializationFormat = "testformat";
    properties.setDefaultSerializationFormat(defaultSerializationFormat);
    instance.preInit(configuration);
    assertThat(configuration.getDefaultSerializationFormat()).isSameAs(defaultSerializationFormat);
  }

  @Test
  public void setDefaultSerializationFormat_ignore_null() {
    final String defaultSerializationFormat = configuration.getDefaultSerializationFormat();
    properties.setDefaultSerializationFormat(null);
    instance.preInit(configuration);
    assertThat(configuration.getDefaultSerializationFormat()).isEqualTo(defaultSerializationFormat);
  }

  @Test
  public void setDefaultSerializationFormat_ignore_empty() {
    final String defaultSerializationFormat = configuration.getDefaultSerializationFormat();
    properties.setDefaultSerializationFormat(" ");
    instance.preInit(configuration);
    assertThat(configuration.getDefaultSerializationFormat()).isEqualTo(defaultSerializationFormat);
  }

  private void initIdGenerator(IdGenerator idGenerator) {
    ReflectionTestUtils.setField(instance, "idGenerator", Optional.ofNullable(idGenerator));
  }
}
