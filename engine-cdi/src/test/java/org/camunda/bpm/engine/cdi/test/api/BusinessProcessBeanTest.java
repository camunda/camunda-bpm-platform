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
package org.camunda.bpm.engine.cdi.test.api;

import static org.junit.Assert.*;

import java.util.Collections;

import javax.enterprise.inject.AmbiguousResolutionException;

import org.junit.Assert;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.ProcessEngineCdiException;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Daniel Meyer
 */
public class BusinessProcessBeanTest extends CdiProcessEngineTestCase {

  /* General test asserting that the business process bean is functional */
  @Test
  @Deployment
  public void test() throws Exception {

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // start the process
    businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // ensure that the process is started:
    assertNotNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());

    // ensure that there is a single task waiting
    Task task = processEngine.getTaskService().createTaskQuery().singleResult();
    assertNotNull(task);

    String value = "value";
    businessProcess.setVariable("key", value);
    assertEquals(value, businessProcess.getVariable("key"));

    // complete the task
    assertEquals(task.getId(), businessProcess.startTask(task.getId()).getId());
    businessProcess.completeTask();

    // assert the task is completed
    assertNull(processEngine.getTaskService().createTaskQuery().singleResult());

    // assert that the process is ended:
    assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());

  }

  @Test
  @Deployment
  public void testProcessWithoutWatestate() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // start the process
    businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // assert that the process is ended:
    assertNull(processEngine.getRuntimeService().createProcessInstanceQuery().singleResult());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResolveProcessInstanceBean() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    assertNull(getBeanInstance(ProcessInstance.class));
    assertNull(getBeanInstance("processInstanceId"));
    assertNull(getBeanInstance(Execution.class));
    assertNull(getBeanInstance("executionId"));

    String pid = businessProcess.startProcessByKey("businessProcessBeanTest").getId();

    // assert that now we can resolve the ProcessInstance-bean
    assertEquals(pid, getBeanInstance(ProcessInstance.class).getId());
    assertEquals(pid, getBeanInstance("processInstanceId"));
    assertEquals(pid, getBeanInstance(Execution.class).getId());
    assertEquals(pid, getBeanInstance("executionId"));

    taskService.complete(taskService.createTaskQuery().singleResult().getId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResolveTaskBean() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    assertNull(getBeanInstance(Task.class));
    assertNull(getBeanInstance("taskId"));


    businessProcess.startProcessByKey("businessProcessBeanTest");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    businessProcess.startTask(taskId);

    // assert that now we can resolve the Task-bean
    assertEquals(taskId, getBeanInstance(Task.class).getId());
    assertEquals(taskId, getBeanInstance("taskId"));

    taskService.complete(taskService.createTaskQuery().singleResult().getId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testGetVariableCache() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // initially the variable cache is empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableCache());

    // set a variable
    businessProcess.setVariable("aVariableName", "aVariableValue");

    // now the variable is set
    assertEquals(Collections.singletonMap("aVariableName", "aVariableValue"), businessProcess.getVariableCache());

    // getting the variable cache does not empty it:
    assertEquals(Collections.singletonMap("aVariableName", "aVariableValue"), businessProcess.getVariableCache());

    businessProcess.startProcessByKey("businessProcessBeanTest");

    // now the variable cache is empty again:
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableCache());

    // set a variable
    businessProcess.setVariable("anotherVariableName", "aVariableValue");

    // now the variable is set
    assertEquals(Collections.singletonMap("anotherVariableName", "aVariableValue"), businessProcess.getVariableCache());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testGetAndClearVariableCache() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // initially the variable cache is empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getAndClearVariableCache());

    // set a variable
    businessProcess.setVariable("aVariableName", "aVariableValue");

    // now the variable is set
    assertEquals(Collections.singletonMap("aVariableName", "aVariableValue"), businessProcess.getAndClearVariableCache());

    // now the variable cache is empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getAndClearVariableCache());

    businessProcess.startProcessByKey("businessProcessBeanTest");

    // now the variable cache is empty again:
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableCache());

    // set a variable
    businessProcess.setVariable("anotherVariableName", "aVariableValue");

    // now the variable is set
    assertEquals(Collections.singletonMap("anotherVariableName", "aVariableValue"), businessProcess.getVariableCache());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testGetVariableLocalCache() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // initially the variable cache is empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableLocalCache());

    // set a variable - this should fail before the process is started
    try {
    	businessProcess.setVariableLocal("aVariableName", "aVariableValue");
      fail("exception expected!");
    }
    catch(ProcessEngineCdiException e) {
      assertEquals("Cannot set a local cached variable: neither a Task nor an Execution is associated.", e.getMessage());
    }

    businessProcess.startProcessByKey("businessProcessBeanTest");

    // now the variable cache is empty again:
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableLocalCache());

    // set a variable
    businessProcess.setVariableLocal("anotherVariableName", "aVariableValue");

    // now the variable is set
    assertEquals(Collections.singletonMap("anotherVariableName", "aVariableValue"), businessProcess.getVariableLocalCache());

    // getting the variable cache does not empty it:
    assertEquals(Collections.singletonMap("anotherVariableName", "aVariableValue"), businessProcess.getVariableLocalCache());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testGetVariableLocal()
  {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    ProcessInstance processInstance = businessProcess.startProcessByKey("businessProcessBeanTest");

    TaskService taskService = getBeanInstance(TaskService.class);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(task);

    businessProcess.startTask(task.getId());

    businessProcess.setVariableLocal("aVariableName", "aVariableValue");

    // Flushing and re-getting should retain the value (CAM-1806):
    businessProcess.flushVariableCache();
    assertTrue(businessProcess.getVariableLocalCache().isEmpty());
    assertEquals("aVariableValue", businessProcess.getVariableLocal("aVariableName"));
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testGetAndClearVariableLocalCache() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // initially the variable cache is empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getAndClearVariableLocalCache());

    // set a variable - this should fail before the process is started
    try {
    	businessProcess.setVariableLocal("aVariableName", "aVariableValue");
      fail("exception expected!");
    }
    catch(ProcessEngineCdiException e) {
      assertEquals("Cannot set a local cached variable: neither a Task nor an Execution is associated.", e.getMessage());
    }

    // the variable cache is still empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getAndClearVariableLocalCache());

    businessProcess.startProcessByKey("businessProcessBeanTest");

    // now the variable cache is empty again:
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableLocalCache());

    // set a variable
    businessProcess.setVariableLocal("anotherVariableName", "aVariableValue");

    // now the variable is set
    assertEquals(Collections.singletonMap("anotherVariableName", "aVariableValue"), businessProcess.getVariableLocalCache());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testFlushVariableCache() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // cannot flush variable cache in absence of an association:
    try {
      businessProcess.flushVariableCache();
      fail("exception expected!");

    } catch (ProcessEngineCdiException e) {
      assertEquals("Cannot flush variable cache: neither a Task nor an Execution is associated.", e.getMessage());

    }

    businessProcess.startProcessByKey("businessProcessBeanTest");

    // set a variable
    businessProcess.setVariable("aVariableName", "aVariable");

    // the variable is not yet present in the execution:
    assertNull(runtimeService.getVariable(businessProcess.getExecutionId(), "aVariableName"));

    // set a local variable
    businessProcess.setVariableLocal("aVariableLocalName", "aVariableLocal");

    // the local variable is not yet present in the execution:
    assertNull(runtimeService.getVariable(businessProcess.getExecutionId(), "aVariableLocalName"));

    // flush the cache
    businessProcess.flushVariableCache();

    // the variable is flushed to the execution
    assertNotNull(runtimeService.getVariable(businessProcess.getExecutionId(), "aVariableName"));

    // the local variable is flushed to the execution
    assertNotNull(runtimeService.getVariable(businessProcess.getExecutionId(), "aVariableLocalName"));

    // the cache is empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableCache());

    // the cache is empty
    assertEquals(Collections.EMPTY_MAP, businessProcess.getVariableLocalCache());

  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testSaveTask() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // cannot save task in absence of an association:
    try {
      businessProcess.saveTask();
      fail();
    } catch (ProcessEngineCdiException e) {
      assertEquals("No task associated. Call businessProcess.startTask() first.", e.getMessage());
    }

    // start the process
    String processInstanceId = businessProcess.startProcessByKey("businessProcessBeanTest", Collections.singletonMap("key", (Object) "value")).getId();
    assertEquals("value", runtimeService.getVariable(processInstanceId, "key"));

    businessProcess.startTask(taskService.createTaskQuery().singleResult().getId());

    // assignee is not set to jonny
    assertNull(taskService.createTaskQuery().taskAssignee("jonny").singleResult());
    Task task = businessProcess.getTask();
    task.setAssignee("jonny");

    assertNull(taskService.createTaskQuery().taskAssignee("jonny").singleResult());

    // if we save the task
    businessProcess.saveTask();

    // THEN

    // assignee is now set to jonny
    assertNotNull(taskService.createTaskQuery().taskAssignee("jonny").singleResult());
    // business process is still associated with task:
    assertTrue(businessProcess.isTaskAssociated());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/cdi/test/api/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testStopTask() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    // cannot stop task in absence of an association:
    try {
      businessProcess.stopTask();
      fail();
    } catch (ProcessEngineCdiException e) {
      assertEquals("No task associated. Call businessProcess.startTask() first.", e.getMessage());
    }

    // start the process
    String processInstanceId = businessProcess.startProcessByKey("businessProcessBeanTest", Collections.singletonMap("key", (Object) "value")).getId();
    assertEquals("value", runtimeService.getVariable(processInstanceId, "key"));

    businessProcess.startTask(taskService.createTaskQuery().singleResult().getId());

    // assignee is not set to jonny
    assertNull(taskService.createTaskQuery().taskAssignee("jonny").singleResult());
    Task task = businessProcess.getTask();
    task.setAssignee("jonny");

    // if we stop the task
    businessProcess.stopTask();

    // THEN

    // assignee is not set to jonny
    assertNull(taskService.createTaskQuery().taskAssignee("jonny").singleResult());
    // business process is not associated with task:
    assertFalse(businessProcess.isTaskAssociated());
    assertFalse(businessProcess.isAssociated());
  }

}
