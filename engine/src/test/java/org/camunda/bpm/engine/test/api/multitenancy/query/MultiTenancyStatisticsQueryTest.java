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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyStatisticsQueryTest extends PluggableProcessEngineTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("EmptyProcess")
    .startEvent().done();

    BpmnModelInstance singleTaskProcess = Bpmn.createExecutableProcess("SingleTaskProcess")
      .startEvent()
        .userTask()
      .done();

    testRule.deploy(process);
    testRule.deployForTenant(TENANT_ONE, singleTaskProcess);
    testRule.deployForTenant(TENANT_TWO, process);
  }

  @Test
  public void testDeploymentStatistics() {
    List<DeploymentStatistics> deploymentStatistics = managementService
        .createDeploymentStatisticsQuery()
        .list();

    assertThat(deploymentStatistics).hasSize(3);

    Set<String> tenantIds = collectDeploymentTenantIds(deploymentStatistics);
    assertThat(tenantIds).contains(null, TENANT_ONE, TENANT_TWO);
  }

  @Test
  public void testProcessDefinitionStatistics() {
    List<ProcessDefinitionStatistics> processDefinitionStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();

    assertThat(processDefinitionStatistics).hasSize(3);

    Set<String> tenantIds = collectDefinitionTenantIds(processDefinitionStatistics);
    assertThat(tenantIds).contains(null, TENANT_ONE, TENANT_TWO);
  }

  @Test
  public void testQueryNoAuthenticatedTenantsForDeploymentStatistics() {
    identityService.setAuthentication("user", null, null);

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();
    assertThat(query.count()).isEqualTo(1L);

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds).hasSize(1);
    assertThat(tenantIds.iterator().next()).isNull();
  }

  @Test
  public void testQueryAuthenticatedTenantForDeploymentStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    assertThat(query.count()).isEqualTo(2L);

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds).hasSize(2);
    assertThat(tenantIds).contains(null, TENANT_ONE);
  }

  @Test
  public void testQueryAuthenticatedTenantsForDeploymentStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    assertThat(query.count()).isEqualTo(3L);

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds).hasSize(3);
    assertThat(tenantIds).contains(null, TENANT_ONE, TENANT_TWO);
  }

  @Test
  public void testQueryDisabledTenantCheckForDeploymentStatistics() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    assertThat(query.count()).isEqualTo(3L);

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds).hasSize(3);
    assertThat(tenantIds).contains(null, TENANT_ONE, TENANT_TWO);
  }

  @Test
  public void testQueryNoAuthenticatedTenantsForProcessDefinitionStatistics() {
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();
    assertThat(query.count()).isEqualTo(1L);

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds).hasSize(1);
    assertThat(tenantIds.iterator().next()).isNull();
  }

  @Test
  public void testQueryAuthenticatedTenantForProcessDefinitionStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    assertThat(query.count()).isEqualTo(2L);

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds).hasSize(2);
    assertThat(tenantIds).contains(null, TENANT_ONE);
  }

  @Test
  public void testQueryAuthenticatedTenantsForProcessDefinitionStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    assertThat(query.count()).isEqualTo(3L);

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds).hasSize(3);
    assertThat(tenantIds).contains(null, TENANT_ONE, TENANT_TWO);
  }

  @Test
  public void testQueryDisabledTenantCheckForProcessDefinitionStatistics() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    assertThat(query.count()).isEqualTo(3L);

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds).hasSize(3);
    assertThat(tenantIds).contains(null, TENANT_ONE, TENANT_TWO);
  }

  @Test
  public void testActivityStatistics() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());

    assertThat(query.count()).isEqualTo(1L);

  }

  @Test
  public void testQueryAuthenticatedTenantForActivityStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());

    assertThat(query.count()).isEqualTo(1L);

  }

  @Test
  public void testQueryNoAuthenticatedTenantForActivityStatistics() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");

    identityService.setAuthentication("user", null);

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());

    assertThat(query.count()).isEqualTo(0L);

  }

  @Test
  public void testQueryDisabledTenantCheckForActivityStatistics() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");

    identityService.setAuthentication("user", null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());

    assertThat(query.count()).isEqualTo(1L);

  }

  protected Set<String> collectDeploymentTenantIds(List<DeploymentStatistics> deploymentStatistics) {
    Set<String> tenantIds = new HashSet<String>();

    for (DeploymentStatistics statistics : deploymentStatistics) {
      tenantIds.add(statistics.getTenantId());
    }
    return tenantIds;
  }

  protected Set<String> collectDefinitionTenantIds(List<ProcessDefinitionStatistics> processDefinitionStatistics) {
    Set<String> tenantIds = new HashSet<String>();

    for (ProcessDefinitionStatistics statistics : processDefinitionStatistics) {
      tenantIds.add(statistics.getTenantId());
    }
    return tenantIds;
  }

}
