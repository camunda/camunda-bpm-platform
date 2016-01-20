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

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class MultiTenancyRuntimeServiceTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ID = "tenant 1";

  protected String deploymentOneId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
        .createDeployment()
        .tenantId(TENANT_ID)
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/twoParallelTasksProcess.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml")
        .deploy()
        .getId();

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentOneId, true);
  }

  public void testPropagateTenantIdToProcessInstance() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance, is(notNullValue()));
    // inherit the tenant id from process definition
    assertThat(processInstance.getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToConcurrentExecution() {
    runtimeService.startProcessInstanceByKey("twoParallelTasksProcess");

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions.size(), is(3));
    assertThat(executions.get(0).getTenantId(), is(TENANT_ID));
    // inherit the tenant id from process instance
    assertThat(executions.get(1).getTenantId(), is(TENANT_ID));
    assertThat(executions.get(2).getTenantId(), is(TENANT_ID));
  }

  public void testPropagateTenantIdToEmbeddedSubprocess() {
    runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions.size(), is(2));
    assertThat(executions.get(0).getTenantId(), is(TENANT_ID));
    // inherit the tenant id from parent execution (e.g. process instance)
    assertThat(executions.get(1).getTenantId(), is(TENANT_ID));
  }

}
