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
package org.camunda.bpm.engine.test.bpmn.servicetask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.test.bpmn.common.AbstractProcessEngineServicesAccessTest;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.Task;

/**
 * @author Daniel Meyer
 *
 */
public class JavaDelegateProcessEngineServicesAccessTest extends AbstractProcessEngineServicesAccessTest {

  protected Class<?> getTestServiceAccessibleClass() {
    return AccessServicesJavaDelegate.class;
  }

  protected Class<?> getQueryClass() {
    return PerformQueryJavaDelegate.class;
  }

  protected Class<?> getStartProcessInstanceClass() {
    return StartProcessJavaDelegate.class;
  }

  protected Task createModelAccessTask(BpmnModelInstance modelInstance, Class<?> delegateClass) {
    ServiceTask serviceTask = modelInstance.newInstance(ServiceTask.class);
    serviceTask.setId("serviceTask");
    serviceTask.setCamundaClass(delegateClass.getName());
    return serviceTask;
  }

  public static class AccessServicesJavaDelegate implements JavaDelegate {
    public void execute(DelegateExecution execution) throws Exception {
      assertCanAccessServices(execution.getProcessEngineServices());
    }
  }

  public static class PerformQueryJavaDelegate implements JavaDelegate {
    public void execute(DelegateExecution execution) throws Exception {
      assertCanPerformQuery(execution.getProcessEngineServices());
    }
  }

  public static class StartProcessJavaDelegate implements JavaDelegate {
    public void execute(DelegateExecution execution) throws Exception {
      assertCanStartProcessInstance(execution.getProcessEngineServices());
    }
  }

}
