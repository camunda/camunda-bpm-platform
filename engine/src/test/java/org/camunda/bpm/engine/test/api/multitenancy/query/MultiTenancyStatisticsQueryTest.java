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

package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyStatisticsQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {

    BpmnModelInstance process = Bpmn.createExecutableProcess("EmptyProcess")
    .startEvent().done();

    BpmnModelInstance singleTaskProcess = Bpmn.createExecutableProcess("SingleTaskProcess")
      .startEvent()
        .userTask()
      .done();

    deployment(process);
    deploymentForTenant(TENANT_ONE, singleTaskProcess);
    deploymentForTenant(TENANT_TWO, process);
  }

  public void testDeploymentStatistics() {
    List<DeploymentStatistics> deploymentStatistics = managementService
        .createDeploymentStatisticsQuery()
        .list();

    assertThat(deploymentStatistics.size(), is(3));

    Set<String> tenantIds = collectDeploymentTenantIds(deploymentStatistics);
    assertThat(tenantIds, hasItems(null, TENANT_ONE, TENANT_TWO));
  }

  public void testProcessDefinitionStatistics() {
    List<ProcessDefinitionStatistics> processDefinitionStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();

    assertThat(processDefinitionStatistics.size(), is(3));

    Set<String> tenantIds = collectDefinitionTenantIds(processDefinitionStatistics);
    assertThat(tenantIds, hasItems(null, TENANT_ONE, TENANT_TWO));
  }

  public void testQueryNoAuthenticatedTenantsForDeploymentStatistics() {
    identityService.setAuthentication("user", null, null);

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();
    assertThat(query.count(), is(1L));

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds.size(), is(1));
    assertThat(tenantIds.iterator().next(), is(nullValue()));
  }

  public void testQueryAuthenticatedTenantForDeploymentStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    assertThat(query.count(), is(2L));

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds.size(), is(2));
    assertThat(tenantIds, hasItems(null, TENANT_ONE));
  }

  public void testQueryAuthenticatedTenantsForDeploymentStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    assertThat(query.count(), is(3L));

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds.size(), is(3));
    assertThat(tenantIds, hasItems(null, TENANT_ONE, TENANT_TWO));
  }

  public void testQueryDisabledTenantCheckForDeploymentStatistics() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DeploymentStatisticsQuery query = managementService.createDeploymentStatisticsQuery();

    assertThat(query.count(), is(3L));

    Set<String> tenantIds = collectDeploymentTenantIds(query.list());
    assertThat(tenantIds.size(), is(3));
    assertThat(tenantIds, hasItems(null, TENANT_ONE, TENANT_TWO));
  }

  public void testQueryNoAuthenticatedTenantsForProcessDefinitionStatistics() {
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();
    assertThat(query.count(), is(1L));

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds.size(), is(1));
    assertThat(tenantIds.iterator().next(), is(nullValue()));
  }

  public void testQueryAuthenticatedTenantForProcessDefinitionStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    assertThat(query.count(), is(2L));

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds.size(), is(2));
    assertThat(tenantIds, hasItems(null, TENANT_ONE));
  }

  public void testQueryAuthenticatedTenantsForProcessDefinitionStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    assertThat(query.count(), is(3L));

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds.size(), is(3));
    assertThat(tenantIds, hasItems(null, TENANT_ONE, TENANT_TWO));
  }

  public void testQueryDisabledTenantCheckForProcessDefinitionStatistics() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionStatisticsQuery query = managementService.createProcessDefinitionStatisticsQuery();

    assertThat(query.count(), is(3L));

    Set<String> tenantIds = collectDefinitionTenantIds(query.list());
    assertThat(tenantIds.size(), is(3));
    assertThat(tenantIds, hasItems(null, TENANT_ONE, TENANT_TWO));
  }

  public void testActivityStatistics() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());

    assertThat(query.count(), is(1L));

  }

  public void testQueryAuthenticatedTenantForActivityStatistics() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());

    assertThat(query.count(), is(1L));

  }

  public void testQueryNoAuthenticatedTenantForActivityStatistics() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");

    identityService.setAuthentication("user", null);

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());

    assertThat(query.count(), is(0L));

  }

  public void testQueryDisabledTenantCheckForActivityStatistics() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SingleTaskProcess");
    
    identityService.setAuthentication("user", null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    ActivityStatisticsQuery query = managementService.createActivityStatisticsQuery(processInstance.getProcessDefinitionId());
    
    assertThat(query.count(), is(1L));
    
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
