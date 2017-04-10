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
package org.camunda.bpm.engine.test.api.history;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Svetlana Dorokhova
 *
 */
public class BulkHistoryDeleteDmnDisabledTest {

  private ProcessEngineImpl processEngineImpl;
  private RepositoryService repositoryService;
  private RuntimeService runtimeService;
  private HistoryService historyService;

  @Before
  public void createProcessEngine() {
    processEngineImpl = createProcessEngineImpl(false);
    repositoryService = processEngineImpl.getRepositoryService();
    runtimeService = processEngineImpl.getRuntimeService();
    historyService = processEngineImpl.getHistoryService();

  }

  // make sure schema is dropped
  @After
  public void cleanup() {
    TestHelper.dropSchema(processEngineImpl.getProcessEngineConfiguration());
    processEngineImpl.close();
    processEngineImpl = null;

  }

  @Test
  public void bulkHistoryDeleteWithDisabledDmn() {
    BpmnModelInstance model = Bpmn.createExecutableProcess("someProcess")
        .startEvent()
          .userTask("userTask")
        .endEvent()
        .done();
    repositoryService.createDeployment().addModelInstance("process.bpmn", model).deploy();
    List<String> ids = prepareHistoricProcesses("someProcess");
    runtimeService.deleteProcessInstances(ids, null, true, true);

    //when
    historyService.deleteHistoricProcessInstancesBulk(ids);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("someProcess").count());
  }

  private List<String> prepareHistoricProcesses(String businessKey) {
    List<String> processInstanceIds = new ArrayList<String>();

    for (int i = 0; i < 5; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(businessKey);
      processInstanceIds.add(processInstance.getId());
    }

    return processInstanceIds;
  }

  protected ProcessEngineImpl createProcessEngineImpl(boolean dmnEnabled) {
    StandaloneInMemProcessEngineConfiguration config =
        (StandaloneInMemProcessEngineConfiguration) new StandaloneInMemProcessEngineConfiguration()
               .setProcessEngineName("database-dmn-test-engine")
               .setDatabaseSchemaUpdate("true")
               .setHistory(ProcessEngineConfiguration.HISTORY_FULL)
               .setJdbcUrl("jdbc:h2:mem:DatabaseDmnTest");

    config.setDmnEnabled(dmnEnabled);

    return (ProcessEngineImpl) config.buildProcessEngine();
  }

}
