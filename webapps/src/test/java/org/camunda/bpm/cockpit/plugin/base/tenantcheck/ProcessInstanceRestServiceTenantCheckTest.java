/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.plugin.base.tenantcheck;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentStatisticsDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.ProcessInstanceRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.junit.Before;
import org.junit.Test;

public class ProcessInstanceRestServiceTenantCheckTest extends AbstractCockpitPluginTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  private ProcessEngine processEngine;
  private ProcessEngineConfiguration processEngineConfiguration;
  private RuntimeService runtimeService;
  private IdentityService identityService;

  private ProcessInstanceRestService resource;
  private ProcessInstanceQueryDto queryParameter;

  @Before
  public void init() throws Exception {

    processEngine = getProcessEngine();
    processEngineConfiguration = getProcessEngine().getProcessEngineConfiguration();

    runtimeService = processEngine.getRuntimeService();
    identityService = processEngine.getIdentityService();

    resource = new ProcessInstanceRestService(processEngine.getName());

    deployForTenant(TENANT_ONE, "processes/failing-process.bpmn");
    deployForTenant(TENANT_TWO, "processes/failing-process.bpmn");

    startProcessInstancesWithTenantId("FailingProcess", TENANT_ONE);
    startProcessInstancesWithTenantId("FailingProcess", TENANT_TWO);

    queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setActivityIdIn(new String[] { "ServiceTask_1" });
  }

  @Test
  public void queryCountNoAuthenticatedTenants() {

    identityService.setAuthentication("user", null, null);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(0);
  }

  @Test
  public void queryCountWithAuthenticatedTenant() {

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(1);
  }

  @Test
  public void queryCountDisabledTenantCheck() {

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  public void queryCountWithCamundaAdmin() {

    identityService.setAuthentication("user", Collections.singletonList(Groups.CAMUNDA_ADMIN), null);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  public void queryWithContainingIncidentsNoAuthenticatedTenants() {

    identityService.setAuthentication("user", null, null);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isEmpty();
  }

  @Test
  public void queryWithContainingIncidentsWithAuthenticatedTenant() {

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    List<IncidentStatisticsDto> incidents = result.get(0).getIncidents();
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);
  }

  @Test
  public void queryWithContainingIncidentsDisabledTenantCheck() {

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    List<IncidentStatisticsDto> incidents = result.get(0).getIncidents();
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);

    incidents = result.get(1).getIncidents();
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);
  }

  @Test
  public void queryWithContainingIncidentsWithCamundaAdmin() {

    identityService.setAuthentication("user", Collections.singletonList(Groups.CAMUNDA_ADMIN), null);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    List<IncidentStatisticsDto> incidents = result.get(0).getIncidents();
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);

    incidents = result.get(1).getIncidents();
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);
  }

  private void startProcessInstancesWithTenantId(String processDefinitionKey, String tenantId) {

    runtimeService
      .createProcessInstanceByKey(processDefinitionKey)
      .processDefinitionTenantId(tenantId)
      .execute();

    executeAvailableJobs();
  }

}
