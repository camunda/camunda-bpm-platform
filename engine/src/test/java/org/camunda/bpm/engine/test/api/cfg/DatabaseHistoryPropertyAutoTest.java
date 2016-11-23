package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.Text;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.camunda.bpm.engine.variable.Variables.putValue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class DatabaseHistoryPropertyAutoTest {

  protected List<ProcessEngineImpl> processEngines = new ArrayList<ProcessEngineImpl>();

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static ProcessEngineConfigurationImpl config(final String historyLevel) {

    return config("false", historyLevel);
  }

  private static ProcessEngineConfigurationImpl config(final String schemaUpdate, final String historyLevel) {
    StandaloneInMemProcessEngineConfiguration engineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    engineConfiguration.setProcessEngineName(UUID.randomUUID().toString());
    engineConfiguration.setDatabaseSchemaUpdate(schemaUpdate);
    engineConfiguration.setHistory(historyLevel);
    engineConfiguration.setDbMetricsReporterActivate(false);
    engineConfiguration.setJdbcUrl("jdbc:h2:mem:DatabaseHistoryPropertyAutoTest");

    return engineConfiguration;
  }


  @Test
  public void failWhenSecondEngineDoesNotHaveTheSameHistoryLevel() {
    buildEngine(config("true", ProcessEngineConfiguration.HISTORY_FULL));

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyLevel mismatch: configuration says HistoryLevelAudit(name=audit, id=2) and database says HistoryLevelFull(name=full, id=3)");

    buildEngine(config(ProcessEngineConfiguration.HISTORY_AUDIT));
  }

  @Test
  public void secondEngineCopiesHistoryLevelFromFirst() {
    // given
    buildEngine(config("true", ProcessEngineConfiguration.HISTORY_FULL));

    // when
    ProcessEngineImpl processEngineTwo = buildEngine(config("true", ProcessEngineConfiguration.HISTORY_AUTO));

    // then
    assertThat(processEngineTwo.getProcessEngineConfiguration().getHistory(), is(ProcessEngineConfiguration.HISTORY_AUTO));
    assertThat(processEngineTwo.getProcessEngineConfiguration().getHistoryLevel(), is(HistoryLevel.HISTORY_LEVEL_FULL));

  }

  @Test
  public void usesDefaultValueAuditWhenNoValueIsConfigured() {
    final ProcessEngineConfigurationImpl config = config("true", ProcessEngineConfiguration.HISTORY_AUTO);
    ProcessEngineImpl processEngine = buildEngine(config);

    final Integer level = config.getCommandExecutorSchemaOperations().execute(new Command<Integer>() {
      @Override
      public Integer execute(CommandContext commandContext) {
        return SchemaOperationsProcessEngineBuild.databaseHistoryLevel(commandContext.getSession(DbEntityManager.class));
      }
    });

    assertThat(level, equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT.getId()));

    assertThat(processEngine.getProcessEngineConfiguration().getHistoryLevel(), equalTo(HistoryLevel.HISTORY_LEVEL_AUDIT));
  }

  @Test
  public void deployProcessOnEngineWithHistoryLevelAuto() throws Exception {
    // given datasource is initialized datasource with history level full
    buildEngine(config(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE, ProcessEngineConfiguration.HISTORY_FULL));

    // and processengine is created with history level auto
    ProcessEngineImpl processEngine = buildEngine(config("false", ProcessEngineConfiguration.HISTORY_AUTO));

    // when a process is deployed to the engine
    BpmnModelInstance process = Bpmn.createExecutableProcess("process").startEvent().userTask("task").endEvent().done();
    processEngine.getRepositoryService().createDeployment().addModelInstance("process.bpmn", process).deploy();

    // then it can be found via repositoryServiceQuery without errors.
    assertThat(processEngine.getRepositoryService()
        .createProcessDefinitionQuery().active()
        .processDefinitionKey("process").singleResult(),
      notNullValue());
    // and when its started, a historyic instance is created
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("process");

    HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processDefinitionKey("process").processInstanceId(processInstance.getId()).singleResult();
    assertThat(historicProcessInstance, notNullValue());
  }

  @Test
  public void evaluateDmnOnEngineWithHistoryAuto() throws Exception {
    // given datasource is initialized datasource with history level full
    buildEngine(config(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE, ProcessEngineConfiguration.HISTORY_FULL));


    // and processengine is created with history level auto
    ProcessEngineImpl processEngine = buildEngine(config("false", ProcessEngineConfiguration.HISTORY_AUTO));

    // when a dmn diagram is deployed to the engine
    DmnModelInstance dmnModelInstance = createDmnModelInstance();
    processEngine.getRepositoryService().createDeployment().addModelInstance("decision.dmn", dmnModelInstance).deploy();


    // then it can be evaluated (the table is empty, so the result is going to be empty)
    DmnDecisionResult result = processEngine.getDecisionService()
      .evaluateDecisionByKey("Decision-1")
      .variables(putValue("input",null))
      .evaluate();

    assertThat(result, notNullValue());
    assertThat(result.size(), is(0));
  }

  protected static DmnModelInstance createDmnModelInstance() {
    DmnModelInstance modelInstance = Dmn.createEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setName(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setNamespace(DmnModelConstants.CAMUNDA_NS);
    modelInstance.setDefinitions(definitions);

    Decision decision = modelInstance.newInstance(Decision.class);
    decision.setId("Decision-1");
    decision.setName("foo");
    modelInstance.getDefinitions().addChildElement(decision);

    DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
    decisionTable.setId(DmnModelConstants.DMN_ELEMENT_DECISION_TABLE);
    decisionTable.setHitPolicy(HitPolicy.FIRST);
    decision.addChildElement(decisionTable);

    Input input = modelInstance.newInstance(Input.class);
    input.setId("Input-1");
    input.setLabel("Input");
    decisionTable.addChildElement(input);

    InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
    inputExpression.setId("InputExpression-1");
    Text inputExpressionText = modelInstance.newInstance(Text.class);
    inputExpressionText.setTextContent("input");
    inputExpression.setText(inputExpressionText);
    inputExpression.setTypeRef("string");
    input.setInputExpression(inputExpression);

    Output output = modelInstance.newInstance(Output.class);
    output.setName("output");
    output.setLabel("Output");
    output.setTypeRef("string");
    decisionTable.addChildElement(output);

    return modelInstance;
  }

  @After
  public void after() {
    for (ProcessEngineImpl engine : processEngines) {
      // no need to drop schema when testing with h2
      for (Deployment deployment : engine.getRepositoryService().createDeploymentQuery().list()) {
        engine.getRepositoryService().deleteDeployment(deployment.getId(), true);
      }
      engine.close();
    }

    processEngines.clear();
  }

  protected ProcessEngineImpl buildEngine(ProcessEngineConfigurationImpl engineConfiguration) {
    ProcessEngineImpl engine = (ProcessEngineImpl) engineConfiguration.buildProcessEngine();
    processEngines.add(engine);

    return engine;
  }

}
