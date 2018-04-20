package org.camunda.bpm.engine.test.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Svetlana Dorokhova
 */
@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricProcessInstanceManagerProcessInstancesForCleanupTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";
  protected static final String TWO_TASKS_PROCESS = "twoTasksProcess";

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;
  private RuntimeService runtimeService;

  @Rule public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
  }

  @Parameterized.Parameter(0)
  public int processDefiniotion1TTL;

  @Parameterized.Parameter(1)
  public int processDefiniotion2TTL;

  @Parameterized.Parameter(2)
  public int processInstancesOfProcess1Count;

  @Parameterized.Parameter(3)
  public int processInstancesOfProcess2Count;

  @Parameterized.Parameter(4)
  public int daysPassedAfterProcessEnd;

  @Parameterized.Parameter(5)
  public int batchSize;

  @Parameterized.Parameter(6)
  public int resultCount;

  @Parameterized.Parameters
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
        { 3, 5, 3, 7, 4, 50, 3 },
        //not enough time has passed
        { 3, 5, 3, 7, 2, 50, 0 },
        //all historic process instances are old enough to be cleaned up
        { 3, 5, 3, 7, 6, 50, 10 },
        //batchSize will reduce the result
        { 3, 5, 3, 7, 6, 4, 4 }
    });
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml" })
  public void testFindHistoricProcessInstanceIdsForCleanup() {

    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {

        //given
        //set different TTL for two process definition
        updateTimeToLive(commandContext, ONE_TASK_PROCESS, processDefiniotion1TTL);
        updateTimeToLive(commandContext, TWO_TASKS_PROCESS, processDefiniotion2TTL);
        return null;
      }
    });
    //start processes
    List<String> ids = prepareHistoricProcesses(ONE_TASK_PROCESS, processInstancesOfProcess1Count);
    ids.addAll(prepareHistoricProcesses(TWO_TASKS_PROCESS, processInstancesOfProcess2Count));

    runtimeService.deleteProcessInstances(ids, null, true, true);

    //some days passed
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), daysPassedAfterProcessEnd));

    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        //when
        List<String> historicProcessInstanceIdsForCleanup = commandContext.getHistoricProcessInstanceManager().findHistoricProcessInstanceIdsForCleanup(
            batchSize, 0, 60);

        //then
        assertEquals(resultCount, historicProcessInstanceIdsForCleanup.size());

        if (resultCount > 0) {

          List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
              .processInstanceIds(new HashSet<String>(historicProcessInstanceIdsForCleanup)).list();

          for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            assertNotNull(historicProcessInstance.getEndTime());
            List<ProcessDefinition> processDefinitions = engineRule.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(historicProcessInstance.getProcessDefinitionId()).list();
            assertEquals(1, processDefinitions.size());
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) processDefinitions.get(0);
            assertTrue(historicProcessInstance.getEndTime().before(DateUtils.addDays(ClockUtil.getCurrentTime(), processDefinition.getHistoryTimeToLive())));
          }
        }

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
