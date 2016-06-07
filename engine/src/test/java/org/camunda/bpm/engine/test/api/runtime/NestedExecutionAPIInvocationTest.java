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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class NestedExecutionAPIInvocationTest {

  @Rule
  public ProcessEngineRule engineRule1 = new ProvidedProcessEngineRule();

  protected ProcessEngine engine2 = createProcessEngine("engine2");

  public static final String PROCESS_KEY_1 = "process";
  
  public static final String PROCESS_KEY_2 = "multiEngineProcess";
  
  public static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";

  public static final BpmnModelInstance PROCESS_MODEL = Bpmn.createExecutableProcess(PROCESS_KEY_1)
      .startEvent()
      .userTask("waitState")
      .serviceTask("startProcess")
        .camundaClass(NestedProcessStartDelegate.class.getName())
      .endEvent()
      .done();
  
  public static final BpmnModelInstance PROCESS_MODEL_2 = Bpmn.createExecutableProcess(PROCESS_KEY_2)
    .startEvent()
    .userTask("waitState")
    .serviceTask("startProcess")
      .camundaClass(StartProcessOnAnotherEngineDelegate.class.getName())
    .endEvent()
    .done();

  public static final BpmnModelInstance ONE_TASK_PROCESS_MODEL = Bpmn.createExecutableProcess(ONE_TASK_PROCESS_KEY)
  .startEvent()
    .userTask("waitState")
  .endEvent()
  .done();

  @Before
  public void init() {
    
    NestedProcessStartDelegate.engine = engineRule1.getProcessEngine();
    StartProcessOnAnotherEngineDelegate.engine = engine2;
    
    // given
    Deployment deployment1 = engineRule1.getRepositoryService()
      .createDeployment()
      .addModelInstance("foo.bpmn", PROCESS_MODEL)
      .deploy();

    Deployment deployment2 = engineRule1.getRepositoryService()
      .createDeployment()
      .addModelInstance("boo.bpmn", PROCESS_MODEL_2)
      .deploy();

    engineRule1.manageDeployment(deployment1);
    engineRule1.manageDeployment(deployment2);
    
    engine2.getRepositoryService()
      .createDeployment()
      .addModelInstance("joo.bpmn", ONE_TASK_PROCESS_MODEL)
      .deploy();
  }

  @After
  public void tearDown() throws Exception {
    if(engine2 != null) {
      engine2.close();
    }
  }

  @Test
  public void testWaitStateIsReachedOnNestedInstantiation() {
    
    engineRule1.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY_1);
    String taskId = engineRule1.getTaskService()
      .createTaskQuery()
      .singleResult()
      .getId();

    // when
    engineRule1.getTaskService().complete(taskId);
  }

  @Test
  public void testWaitStateIsReachedOnMultiEngine() {

    engineRule1.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY_2);
    String taskId = engineRule1.getTaskService()
      .createTaskQuery()
      .singleResult()
      .getId();

    // when
    engineRule1.getTaskService().complete(taskId);
  }

  public static class StartProcessOnAnotherEngineDelegate implements JavaDelegate {

    public static ProcessEngine engine;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      
      RuntimeService runtimeService = engine.getRuntimeService();

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

      // then the wait state is reached immediately after instantiation
      ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
      ActivityInstance[] activityInstances = activityInstance.getActivityInstances("waitState");
      Assert.assertEquals(1, activityInstances.length);

    }
  }

  public static class NestedProcessStartDelegate implements JavaDelegate {
    
    public static ProcessEngine engine;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      
      RuntimeService runtimeService = engine.getRuntimeService();
      
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
      
      // then the wait state is reached immediately after instantiation
      ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
      ActivityInstance[] activityInstances = activityInstance.getActivityInstances("waitState");
      Assert.assertEquals(1, activityInstances.length);
      
    }
  }

  protected ProcessEngine createProcessEngine(String name) {

    StandaloneInMemProcessEngineConfiguration processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setProcessEngineName(name);
    processEngineConfiguration.setJdbcUrl(String.format("jdbc:h2:mem%s",name));
    return processEngineConfiguration.buildProcessEngine();
   
  }
}
