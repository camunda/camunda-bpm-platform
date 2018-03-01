package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.test.util.ReflectionTestUtils;

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
    when(filterQuery.singleResult()).thenReturn(filter);

    configuration.postProcessEngineBuild(engine);

    verify(filterService).createFilterQuery();
    verify(filterQuery).filterName("All");
    verify(filterService, never()).newTaskFilter("All");

  }
}
