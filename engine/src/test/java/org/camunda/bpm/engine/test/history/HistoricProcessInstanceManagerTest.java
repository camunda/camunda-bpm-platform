package org.camunda.bpm.engine.test.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static org.junit.Assert.assertEquals;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricProcessInstanceManagerTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";
  protected static final String TWO_TASKS_PROCESS = "twoTasksProcess";

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private RuntimeService runtimeService;

  @Rule public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml" })
  public void testCountHistoricProcessInstanceIdsForCleanup() {

    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        //given
        //set different TTL for two process definition
        updateTimeToLive(commandContext, ONE_TASK_PROCESS, 3);
        updateTimeToLive(commandContext, TWO_TASKS_PROCESS, 5);
        return null;
      }
    });
    //start processes
    List<String> ids = prepareHistoricProcesses(ONE_TASK_PROCESS, 35);
    ids.addAll(prepareHistoricProcesses(TWO_TASKS_PROCESS, 65));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //4 days passed
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), 4));

    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        //when
        Long count = commandContext.getHistoricProcessInstanceManager().findHistoricProcessInstanceIdsForCleanupCount();

        //then
        assertEquals(35L, count.longValue());

        return null;
      }
    });

  }

  private void updateTimeToLive(CommandContext commandContext, String businessKey, int timeToLive) {
    List<ProcessDefinition> processDefinitions = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(businessKey).list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinitionEntity processDefinition1 = (ProcessDefinitionEntity) processDefinitions.get(0);
    processDefinition1.setHistoryTimeToLive(timeToLive);
    commandContext.getDbEntityManager().merge(processDefinition1);
  }

  private List<String> prepareHistoricProcesses(String businessKey, Integer processInstanceCount) {
    List<String> processInstanceIds = new ArrayList<String>();

    for (int i = 0; i < processInstanceCount; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(businessKey);
      processInstanceIds.add(processInstance.getId());
    }

    return processInstanceIds;
  }

}
