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
package org.camunda.bpm.cockpit.plugin.base.tenantcheck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.IncidentQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.IncidentRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IncidentRestServiceTenantCheckTest extends AbstractCockpitPluginTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";
  protected static final String ADMIN_GROUP = "adminGroup";
  protected static final String ADMIN_USER = "adminUser";

  private ProcessEngine processEngine;
  private ProcessEngineConfigurationImpl processEngineConfiguration;
  private RuntimeService runtimeService;
  private IdentityService identityService;

  private IncidentRestService resource;
  private IncidentQueryDto queryParameter;

  private String processInstanceTenantOne;
  private String processInstanceTenantTwo;

  @Before
  public void init() throws Exception {

    processEngine = getProcessEngine();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    runtimeService = processEngine.getRuntimeService();
    identityService = processEngine.getIdentityService();

    processEngineConfiguration.getAdminGroups().add(ADMIN_GROUP);
    processEngineConfiguration.getAdminUsers().add(ADMIN_USER);

    resource = new IncidentRestService(processEngine.getName());

    deployForTenant(TENANT_ONE, "processes/failing-process.bpmn");
    deployForTenant(TENANT_TWO, "processes/failing-process.bpmn");

    processInstanceTenantOne = runtimeService.createProcessInstanceByKey("FailingProcess")
        .processDefinitionTenantId(TENANT_ONE).execute().getId();

    processInstanceTenantTwo = runtimeService.createProcessInstanceByKey("FailingProcess")
        .processDefinitionTenantId(TENANT_TWO).execute().getId();

    executeAvailableJobs();

    queryParameter = new IncidentQueryDto();
    queryParameter.setProcessInstanceIdIn(new String[]{processInstanceTenantOne, processInstanceTenantTwo});
  }

  @After
  public void tearDown() {
    processEngineConfiguration.getAdminGroups().remove(ADMIN_GROUP);
    processEngineConfiguration.getAdminUsers().remove(ADMIN_USER);
  }

  @Test
  public void queryIncidentsByProcessInstanceIdsNoAuthenticatedTenants() {

    identityService.setAuthentication("user", null, null);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isEmpty();
  }

  @Test
  public void queryIncidentsByProcessInstanceIdsWithAuthenticatedTenant() {

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    IncidentDto incident = result.get(0);
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstanceTenantOne);
  }

  @Test
  public void queryIncidentsByProcessInstanceIdsDisabledTenantCheck() {

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).hasSize(2);

    Set<String> processInstnaceIds = new HashSet<>();
    for (IncidentDto incidentDto : result) {
      processInstnaceIds.add(incidentDto.getProcessInstanceId());
    }

    assertThat(processInstnaceIds).contains(processInstanceTenantOne);
    assertThat(processInstnaceIds).contains(processInstanceTenantTwo);
  }

  @Test
  public void queryIncidentsByProcessInstanceIdsWithCamundaAdmin() {

    identityService.setAuthentication("user", Collections.singletonList(Groups.CAMUNDA_ADMIN), null);


    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).hasSize(2);

    Set<String> processInstnaceIds = new HashSet<>();
    for (IncidentDto incidentDto : result) {
      processInstnaceIds.add(incidentDto.getProcessInstanceId());
    }

    assertThat(processInstnaceIds).contains(processInstanceTenantOne);
    assertThat(processInstnaceIds).contains(processInstanceTenantTwo);
  }

  @Test
  public void queryIncidentsByProcessInstanceIdsWithAdminGroups() {

    identityService.setAuthentication("user", Collections.singletonList(ADMIN_GROUP), null);


    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).hasSize(2);

    Set<String> processInstnaceIds = new HashSet<>();
    for (IncidentDto incidentDto : result) {
      processInstnaceIds.add(incidentDto.getProcessInstanceId());
    }

    assertThat(processInstnaceIds).contains(processInstanceTenantOne);
    assertThat(processInstnaceIds).contains(processInstanceTenantTwo);
  }

  @Test
  public void queryIncidentsByProcessInstanceIdsWithAdminUsers() {

    identityService.setAuthentication("adminUser", null, null);


    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).hasSize(2);

    Set<String> processInstnaceIds = new HashSet<>();
    for (IncidentDto incidentDto : result) {
      processInstnaceIds.add(incidentDto.getProcessInstanceId());
    }

    assertThat(processInstnaceIds).contains(processInstanceTenantOne);
    assertThat(processInstnaceIds).contains(processInstanceTenantTwo);
  }

}
