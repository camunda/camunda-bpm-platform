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
package org.camunda.bpm.engine.test.api.runtime;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
@Ignore("CAM-5618")
public class NestedExecutionAPIInvocationTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  public static final String PROCESS_KEY = "process";

  public static final BpmnModelInstance PROCESS_MODEL = Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent()
      .serviceTask("passThrough")
        .camundaExpression("${true}")
      .userTask("waitState")
      .serviceTask("startProcess")
        .camundaClass(NestedProcessStartDelegate.class.getName())
      .endEvent()
      .done();

  @Test
  @Ignore("CAM-5618")
  public void testWaitStateIsReachedOnNestedInstantiation() {
    // given
    Deployment deployment = engineRule
      .getRepositoryService()
      .createDeployment()
      .addModelInstance("foo.bpmn", PROCESS_MODEL)
      .deploy();
    engineRule.manageDeployment(deployment);

    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY);
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();

    // when
    engineRule.getTaskService().complete(task.getId());
  }

  public static class NestedProcessStartDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);

      // then the wait state is reached immediately after instantiation
      ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
      ActivityInstance[] activityInstances = activityInstance.getActivityInstances("waitState");
      Assert.assertEquals(1, activityInstances.length);

    }
  }
}
