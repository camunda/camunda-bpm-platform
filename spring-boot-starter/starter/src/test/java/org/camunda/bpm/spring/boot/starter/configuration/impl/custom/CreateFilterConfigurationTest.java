/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEngineLogger;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class CreateFilterConfigurationTest {

  private final CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();

  {
    camundaBpmProperties.getFilter().setCreate("All");
  }

  private final CreateFilterConfiguration configuration = new CreateFilterConfiguration();

  {
    ReflectionTestUtils.setField(configuration, "camundaBpmProperties", camundaBpmProperties);
    configuration.init();
  }

  @Rule
  public final ProcessEngineRule processEngineRule = new StandaloneInMemoryTestConfiguration(configuration).rule();

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(SpringBootProcessEngineLogger.PACKAGE);

  @Test
  public void createAdminUser() throws Exception {
    assertThat(processEngineRule.getFilterService().createFilterQuery().filterName("All").singleResult()).isNotNull();
  }

  @Test
  public void fail_if_not_configured_onInit() throws Exception {
    thrown.expect(IllegalStateException.class);
    CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();
    final CreateFilterConfiguration configuration = new CreateFilterConfiguration();
    ReflectionTestUtils.setField(configuration, "camundaBpmProperties", camundaBpmProperties);
    configuration.init();
  }

  @Test
  public void fail_if_not_configured_onExecution() throws Exception {
    thrown.expect(NullPointerException.class);

    CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();
    camundaBpmProperties.getFilter().setCreate("All");
    final CreateFilterConfiguration configuration = new CreateFilterConfiguration();
    ReflectionTestUtils.setField(configuration, "camundaBpmProperties", camundaBpmProperties);
    configuration.init();
    configuration.filterName = null;

    configuration.postProcessEngineBuild(mock(ProcessEngine.class));
  }

  @Test
  public void do_not_create_when_already_exist() throws Exception {
    CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();
    camundaBpmProperties.getFilter().setCreate("All");
    final CreateFilterConfiguration configuration = new CreateFilterConfiguration();
    ReflectionTestUtils.setField(configuration, "camundaBpmProperties", camundaBpmProperties);
    configuration.init();

    ProcessEngine engine = mock(ProcessEngine.class);
    FilterService filterService = mock(FilterService.class);
    FilterQuery filterQuery = mock(FilterQuery.class);
    Filter filter = mock(Filter.class);

    when(engine.getFilterService()).thenReturn(filterService);
    when(filterService.createFilterQuery()).thenReturn(filterQuery);
    when(filterQuery.filterName(anyString())).thenReturn(filterQuery);
    when(filterQuery.count()).thenReturn(1L);

    configuration.postProcessEngineBuild(engine);

    verifyLogs(Level.INFO, "the filter with this name already exists");
    verify(filterService).createFilterQuery();
    verify(filterQuery).filterName("All");
    verify(filterService, never()).newTaskFilter("All");
  }

  protected void verifyLogs(Level logLevel, String message) {
    List<ILoggingEvent> logs = loggingRule.getLog();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(logLevel);
    assertThat(logs.get(0).getMessage()).containsIgnoringCase(message);
  }
}