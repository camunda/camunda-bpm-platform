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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.CalledProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.CalledProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessInstanceResource;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

public class ProcessInstanceResourceTenantCheckTest extends AbstractCockpitPluginTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  private ProcessInstanceResource resource;
  private ProcessEngine processEngine;
  private ProcessEngineConfiguration processEngineConfiguration;
  private RuntimeService runtimeService;
  private IdentityService identityService;
  private CalledProcessInstanceQueryDto queryParameter;

  @Before
  public void init() throws Exception {

    processEngine = getProcessEngine();
    processEngineConfiguration = getProcessEngine().getProcessEngineConfiguration();
    runtimeService = processEngine.getRuntimeService();
    identityService = processEngine.getIdentityService();

    deploy("processes/multi-tenancy-call-activity.bpmn");
    deployForTenant(TENANT_ONE, "processes/user-task-process.bpmn");
    deployForTenant(TENANT_TWO, "processes/user-task-process.bpmn");

    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey("multiTenancyCallActivity").execute();

    resource = new ProcessInstanceResource(getProcessEngine().getName(), processInstance.getId());

    queryParameter = new CalledProcessInstanceQueryDto();
  }

  @Test
  public void getCalledProcessInstancesByParentProcessInstanceIdNoAuthenticatedTenants() {

    identityService.setAuthentication("user", null, null);

    List<CalledProcessInstanceDto> result = resource.queryCalledProcessInstances(queryParameter);
    assertThat(result).isEmpty();
  }

  @Test
  public void getCalledProcessInstancesByParentProcessInstanceIdWithAuthenticatedTenant() {

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<CalledProcessInstanceDto> result = resource.queryCalledProcessInstances(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    CalledProcessInstanceDto dto = result.get(0);
    assertThat(dto.getCallActivityId()).isEqualTo("CallActivity_Tenant1");
  }

  @Test
  public void getCalledProcessInstancesByParentProcessInstanceIdDisabledTenantCheck() {

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<CalledProcessInstanceDto> result = resource.queryCalledProcessInstances(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    assertThat(getCalledActivityIds(result), hasItems("CallActivity_Tenant1", "CallActivity_Tenant2"));
  }

  @Test
  public void getCalledProcessInstancesByParentProcessInstanceIdWithCamundaAdmin() {

    identityService.setAuthentication("user", Collections.singletonList(Groups.CAMUNDA_ADMIN), null);

    List<CalledProcessInstanceDto> result = resource.queryCalledProcessInstances(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    assertThat(getCalledActivityIds(result), hasItems("CallActivity_Tenant1", "CallActivity_Tenant2"));
  }

  private Set<String> getCalledActivityIds(List<CalledProcessInstanceDto> result) {
    Set<String> callActivityIds = new HashSet<String>();
    for (CalledProcessInstanceDto calledProcessInstanceDto : result) {
       callActivityIds.add(calledProcessInstanceDto.getCallActivityId());
    }
    return callActivityIds;
  }

}
