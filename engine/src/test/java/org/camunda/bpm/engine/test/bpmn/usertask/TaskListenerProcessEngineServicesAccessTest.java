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
package org.camunda.bpm.engine.test.bpmn.usertask;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.test.bpmn.common.AbstractProcessEngineServicesAccessTest;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;

/**
 * @author Daniel Meyer
 *
 */
public class TaskListenerProcessEngineServicesAccessTest extends AbstractProcessEngineServicesAccessTest {

  protected Class<?> getTestServiceAccessibleClass() {
    return AccessServicesListener.class;
  }

  protected Class<?> getQueryClass() {
    return PerformQueryListener.class;
  }

  protected Class<?> getStartProcessInstanceClass() {
    return StartProcessListener.class;
  }

  protected Class<?> getProcessEngineStartProcessClass() {
    return ProcessEngineStartProcessListener.class;
  }

  protected Task createModelAccessTask(BpmnModelInstance modelInstance, Class<?> delegateClass) {
    UserTask task = modelInstance.newInstance(UserTask.class);
    task.setId("userTask");
    CamundaTaskListener executionListener = modelInstance.newInstance(CamundaTaskListener.class);
    executionListener.setCamundaEvent(TaskListener.EVENTNAME_CREATE);
    executionListener.setCamundaClass(delegateClass.getName());
    task.builder().addExtensionElement(executionListener);
    return task;
  }

  public static class AccessServicesListener implements TaskListener {
    public void notify(DelegateTask execution) {
      assertCanAccessServices(execution.getProcessEngineServices());
    }
  }

  public static class PerformQueryListener implements TaskListener {
    public void notify(DelegateTask execution) {
      assertCanPerformQuery(execution.getProcessEngineServices());
    }
  }

  public static class StartProcessListener implements TaskListener {
    public void notify(DelegateTask execution) {
      assertCanStartProcessInstance(execution.getProcessEngineServices());
    }
  }

  public static class ProcessEngineStartProcessListener implements TaskListener {
    @Override
    public void notify(DelegateTask execution) {
      assertCanStartProcessInstance(execution.getProcessEngine());
    }
  }

}
