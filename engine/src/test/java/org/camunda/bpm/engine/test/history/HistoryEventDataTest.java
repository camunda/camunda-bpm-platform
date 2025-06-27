package org.camunda.bpm.engine.test.history;

import static org.camunda.bpm.engine.impl.util.StringUtil.hasText;

import java.util.Objects;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryEventDataTest {

  private static final TestEventHandler HANDLER = new TestEventHandler();

  @Rule
  public HistoryEventVerifier verifier = new HistoryEventVerifier(HANDLER);

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      c -> c.setHistoryEventHandler(HANDLER));

  private RuntimeService runtimeService;

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();

    verifier.historyEventIs("!= null", Objects::nonNull);
    verifier.historyEventHas("processDefinitionId != null", (evt) -> hasText(evt.getProcessDefinitionId()));
    verifier.historyEventHas("processDefinitionKey != null", (evt) -> hasText(evt.getProcessDefinitionKey()));
    verifier.historyEventHas("processDefinitionName != null", (evt) -> hasText(evt.getProcessDefinitionName()));
    verifier.historyEventHas("processDefinitionVersion != null", (evt) -> evt.getProcessDefinitionVersion() != null);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/threeTasksProcess.bpmn20.xml")
  public void verify() {
    runtimeService.startProcessInstanceByKey("threeTasksProcess");
  }
}