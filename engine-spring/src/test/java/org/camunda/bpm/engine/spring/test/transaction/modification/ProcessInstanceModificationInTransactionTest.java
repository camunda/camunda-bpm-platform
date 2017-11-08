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
package org.camunda.bpm.engine.spring.test.transaction.modification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:org/camunda/bpm/engine/spring/test/transaction/ProcessInstanceModificationInTransactionTest-applicationContext.xml"})
public class ProcessInstanceModificationInTransactionTest {

  @Autowired
  @Rule
  public ProcessEngineRule rule;

  @Autowired
  public ProcessEngine processEngine;

  @Autowired
  RuntimeService runtimeService;

  @Autowired
  RepositoryService repositoryService;

  @Autowired
  UserBean userBean;

  @Before
  public void init() {
    LogFactory.useSlf4jLogging();
  }

  @Test
  public void shouldBeAbleToPerformModification() {

    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("TestProcess")
      .startEvent()
      .intermediateCatchEvent("TimerEvent")
        .timerWithDate("${calculateTimerDate.execute(execution)}")
        .camundaExecutionListenerDelegateExpression("end", "${deleteVariableListener}")
      .endEvent()
      .done();

    deployModelInstance(modelInstance);
    final ProcessInstance procInst = runtimeService.startProcessInstanceByKey("TestProcess");

    // when
    userBean.completeUserTaskAndModifyInstanceInOneTransaction(procInst);

    // then
    VariableInstance variable = rule.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(procInst.getId()).variableName("createDate").singleResult();
    assertNotNull(variable);
    HistoricVariableInstance historicVariable = rule.getHistoryService().createHistoricVariableInstanceQuery().singleResult();
    assertEquals(variable.getName(), historicVariable.getName());
    assertEquals(HistoricVariableInstance.STATE_CREATED, historicVariable.getState());
  }

  private void deployModelInstance(BpmnModelInstance modelInstance) {
    DeploymentBuilder deploymentbuilder = repositoryService.createDeployment();
    deploymentbuilder.addModelInstance("process0.bpmn", modelInstance);
    Deployment deployment = deploymentbuilder.deploy();
    rule.manageDeployment(deployment);
  }
}
