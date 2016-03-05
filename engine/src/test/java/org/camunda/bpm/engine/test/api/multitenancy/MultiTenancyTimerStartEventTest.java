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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyTimerStartEventTest extends PluggableProcessEngineTestCase {

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess()
      .startEvent()
        .timerWithDuration("PT1M")
      .userTask()
      .endEvent()
      .done();

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  public void testStartProcessInstanceWithTenantId() {

    deploymentForTenant(TENANT_ONE, PROCESS);

    Job job = managementService.createJobQuery().singleResult();
    assertThat(job.getTenantId(), is(TENANT_ONE));

    managementService.executeJob(job.getId());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance, is(notNullValue()));
    assertThat(processInstance.getTenantId(), is(TENANT_ONE));
  }

  public void testStartProcessInstanceTwoTenants() {

    deploymentForTenant(TENANT_ONE, PROCESS);
    deploymentForTenant(TENANT_TWO, PROCESS);

    Job jobForTenantOne = managementService.createJobQuery().tenantIdIn(TENANT_ONE).singleResult();
    assertThat(jobForTenantOne, is(notNullValue()));
    managementService.executeJob(jobForTenantOne.getId());

    Job jobForTenantTwo = managementService.createJobQuery().tenantIdIn(TENANT_TWO).singleResult();
    assertThat(jobForTenantTwo, is(notNullValue()));
    managementService.executeJob(jobForTenantTwo.getId());

    assertThat(runtimeService.createProcessInstanceQuery().tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(runtimeService.createProcessInstanceQuery().tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testDeleteJobsWhileUndeployment() {

     String deploymentForTenantOne = deploymentForTenant(TENANT_ONE, PROCESS);
     String deploymentForTenantTwo = deploymentForTenant(TENANT_TWO, PROCESS);

     JobQuery query = managementService.createJobQuery();
     assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
     assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));

     repositoryService.deleteDeployment(deploymentForTenantOne, true);

     assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
     assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));

     repositoryService.deleteDeployment(deploymentForTenantTwo, true);

     assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
     assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  public void testDontCreateNewJobsWhileReDeployment() {

    deploymentForTenant(TENANT_ONE, PROCESS);
    deploymentForTenant(TENANT_TWO, PROCESS);
    deploymentForTenant(TENANT_ONE, PROCESS);

    JobQuery query = managementService.createJobQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

}
