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
package org.camunda.bpm.engine.test.bpmn.common;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.Assert;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractProcessEngineServicesAccessTest extends PluggableProcessEngineTestCase {

  private static final String TASK_DEF_KEY = "someTask";

  private static final String PROCESS_DEF_KEY = "testProcess";

  private static final String CALLED_PROCESS_DEF_ID = "calledProcess";

  protected List<String> deploymentIds = new ArrayList<String>();

  @Override
  protected void tearDown() throws Exception {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
    super.tearDown();
  }

  public void testServicesAccessible() {
    // this test makes sure that the process engine services can be accessed and are non-null.
    createAndDeployModelForClass(getTestServiceAccessibleClass());

    // this would fail if api access was not assured.
    runtimeService.startProcessInstanceByKey(PROCESS_DEF_KEY);
  }

  public void testQueryAccessible() {
    // this test makes sure we can perform a query
    createAndDeployModelForClass(getQueryClass());

    // this would fail if api access was not assured.
    runtimeService.startProcessInstanceByKey(PROCESS_DEF_KEY);
  }

  public void testStartProcessInstance() {

    // given
    createAndDeployModelForClass(getStartProcessInstanceClass());

    deployModel(Bpmn.createExecutableProcess(CALLED_PROCESS_DEF_ID)
      .startEvent()
      .userTask(TASK_DEF_KEY)
      .endEvent()
    .done());

    // if
    runtimeService.startProcessInstanceByKey(PROCESS_DEF_KEY);

    // then
    // the started process instance is still active and waiting at the user task
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_DEF_KEY).count());
  }

  public void testStartProcessInstanceFails() {

    // given
    createAndDeployModelForClass(getStartProcessInstanceClass());

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CALLED_PROCESS_DEF_ID)
        .startEvent()
        .scriptTask("scriptTask")
          .scriptFormat("groovy")
          .scriptText("throw new RuntimeException(\"BOOOM!\")")
        .endEvent()
      .done();

    deployModel(modelInstance);

    // if
    try {
      runtimeService.startProcessInstanceByKey(PROCESS_DEF_KEY);
      fail("exception expected");
    } catch(RuntimeException e) {
      assertTextPresent("BOOOM", e.getMessage());
    }

    // then
    // starting the process fails and everything is rolled back:
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  protected abstract Class<?> getTestServiceAccessibleClass();

  protected abstract Class<?> getQueryClass();

  protected abstract Class<?> getStartProcessInstanceClass();

  protected abstract Task createModelAccessTask(BpmnModelInstance modelInstance, Class<?> delegateClass);

  // Helper methods //////////////////////////////////////////////

  private void createAndDeployModelForClass(Class<?> delegateClass) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_DEF_KEY)
      .startEvent()
      .manualTask("templateTask")
      .endEvent()
    .done();

    // replace the template task with the actual task provided by the subtask
    modelInstance.getModelElementById("templateTask")
      .replaceWithElement(createModelAccessTask(modelInstance, delegateClass));

    deployModel(modelInstance);
  }


  private void deployModel(BpmnModelInstance model) {
    Deployment deployment = repositoryService.createDeployment().addModelInstance("testProcess.bpmn", model).deploy();
    deploymentIds.add(deployment.getId());
  }


  public static void assertCanAccessServices(ProcessEngineServices services) {
    Assert.assertNotNull(services.getAuthorizationService());
    Assert.assertNotNull(services.getFormService());
    Assert.assertNotNull(services.getHistoryService());
    Assert.assertNotNull(services.getIdentityService());
    Assert.assertNotNull(services.getManagementService());
    Assert.assertNotNull(services.getRepositoryService());
    Assert.assertNotNull(services.getRuntimeService());
    Assert.assertNotNull(services.getTaskService());
  }

  public static void assertCanPerformQuery(ProcessEngineServices services) {
    services.getRepositoryService()
      .createProcessDefinitionQuery()
      .count();
  }

  public static void assertCanStartProcessInstance(ProcessEngineServices services) {
    services.getRuntimeService().startProcessInstanceByKey(CALLED_PROCESS_DEF_ID);
  }

}
