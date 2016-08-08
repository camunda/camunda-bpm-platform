package org.camunda.bpm.cockpit.plugin.base.tenantcheck;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessDefinitionQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessDefinitionResource;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

public class ProcessDefinitionResourceTenantCheckTest extends AbstractCockpitPluginTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  private ProcessEngine processEngine;
  private ProcessEngineConfiguration processEngineConfiguration;
  private RuntimeService runtimeService;
  private IdentityService identityService;

  private ProcessDefinitionResource resource;
  private ProcessDefinitionQueryDto queryParameter;

  @Before
  public void init() throws Exception {

    processEngine = getProcessEngine();
    processEngineConfiguration = processEngine.getProcessEngineConfiguration();

    runtimeService = processEngine.getRuntimeService();
    identityService = processEngine.getIdentityService();

    deploy("processes/multi-tenancy-call-activity.bpmn");
    deployForTenant(TENANT_ONE, "processes/user-task-process.bpmn");
    deployForTenant(TENANT_TWO, "processes/user-task-process.bpmn");

    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey("multiTenancyCallActivity").execute();

    resource = new ProcessDefinitionResource(getProcessEngine().getName(), processInstance.getProcessDefinitionId());

    queryParameter = new ProcessDefinitionQueryDto();
  }

  @Test
  public void calledProcessDefinitionByParentProcessDefinitionIdNoAuthenticatedTenant() {

    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isEmpty();
  }

  @Test
  public void calledProcessDefinitionByParentProcessDefinitionIdWithAuthenticatedTenant() {

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    assertThat(getCalledFromActivityIds(result)).containsOnly("CallActivity_Tenant1");
  }

  @Test
  public void calledProcessDefinitionByParentProcessDefinitionIdDisabledTenantCheck() {

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    assertThat(getCalledFromActivityIds(result)).contains("CallActivity_Tenant1", "CallActivity_Tenant2");
  }

  @Test
  public void calledProcessDefinitionByParentProcessDefinitionIdWithCamundaAdmin() {

    identityService.setAuthentication("user", Collections.singletonList(Groups.CAMUNDA_ADMIN), null);

    List<ProcessDefinitionDto> result = resource.queryCalledProcessDefinitions(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    assertThat(getCalledFromActivityIds(result)).contains("CallActivity_Tenant1", "CallActivity_Tenant2");
  }

  protected List<String> getCalledFromActivityIds(List<ProcessDefinitionDto> processDefinitions) {
    List<String> activityIds = new ArrayList<String>();

    for (ProcessDefinitionDto processDefinition : processDefinitions) {
      activityIds.addAll(processDefinition.getCalledFromActivityIds());
    }

    return activityIds;
  }


}
