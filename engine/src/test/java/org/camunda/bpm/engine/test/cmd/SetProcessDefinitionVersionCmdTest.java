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
package org.camunda.bpm.engine.test.cmd;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Ingo Richtsmeier
 *
 */
public class SetProcessDefinitionVersionCmdTest extends PluggableProcessEngineTestCase {
  
  static {
    LogFactory.useJdkLogging();
  }
  
  public void testHistoryOfSetProcessDefinitionVersionCmd() {
    String resource = "org/camunda/bpm/engine/test/cmd/SetProcessDefinitionVersionCmdTest.bpmn";
    Deployment deployVersion1 = repositoryService
        .createDeployment()
        .addClasspathResource(resource)
        .deploy();
    Deployment deployVersion2 = repositoryService
        .createDeployment()
        .addClasspathResource(resource)
        .enableDuplicateFiltering(false)
        .deploy();
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(deployVersion1.getId())
        .singleResult();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    SetProcessDefinitionVersionCmd command = new SetProcessDefinitionVersionCmd(processInstance.getId(), 2);
    processEngineConfiguration.getCommandExecutorTxRequired().execute(command);

    HistoricProcessInstance historicProcessInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();
    ProcessDefinition targetdDefinition = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(deployVersion2.getId())
        .singleResult();

    assertEquals(targetdDefinition.getId(), historicProcessInstance.getProcessDefinitionId());
    
    // Clean up the test
    repositoryService.deleteDeployment(deployVersion1.getId(), true);
    repositoryService.deleteDeployment(deployVersion2.getId(), true);
  }
  
}
