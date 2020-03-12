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
package org.camunda.bpm.engine.test.api.resources;

import static org.camunda.bpm.engine.repository.ResourceTypes.HISTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.api.variables.JavaSerializable;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryByteArrayTest {
  protected static final String DECISION_PROCESS = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";
  protected static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";
  protected static final String WORKER_ID = "aWorkerId";
  protected static final long LOCK_TIME = 10000L;
  protected static final String TOPIC_NAME = "externalTaskTopic";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ExternalTaskService externalTaskService;

  protected String taskId;

  @Before
  public void initServices() {
    configuration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    externalTaskService = engineRule.getExternalTaskService();
  }

  @After
  public void tearDown() {
    if (taskId != null) {
      // delete task
      taskService.deleteTask(taskId, true);
    }
  }

  @Before
  public void enableDmnFeelLegacyBehavior() {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
            .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();
  }

  @After
  public void disableDmnFeelLegacyBehavior() {

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
            .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();
  }

  @Test
  public void testHistoricVariableBinaryForFileValues() {
    // given
    BpmnModelInstance instance = createProcess();

    testRule.deploy(instance);
    FileValue fileValue = createFile();

    runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValueTyped("fileVar", fileValue));

    String byteArrayValueId = ((HistoricVariableInstanceEntity)historyService.createHistoricVariableInstanceQuery().singleResult()).getByteArrayValueId();

    // when
    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired()
        .execute(new GetByteArrayCommand(byteArrayValueId));

    checkBinary(byteArrayEntity);
  }

  @Test
  public void testHistoricVariableBinary() {
    byte[] binaryContent = "some binary content".getBytes();

    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("binaryVariable", binaryContent);
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskId = task.getId();
    taskService.setVariablesLocal(taskId, variables);

    String byteArrayValueId = ((HistoricVariableInstanceEntity)historyService.createHistoricVariableInstanceQuery().singleResult()).getByteArrayValueId();

    // when
    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired()
        .execute(new GetByteArrayCommand(byteArrayValueId));

    checkBinary(byteArrayEntity);
  }

  @Test
  public void testHistoricDetailBinaryForFileValues() {
    // given
    BpmnModelInstance instance = createProcess();

    testRule.deploy(instance);
    FileValue fileValue = createFile();

    runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValueTyped("fileVar", fileValue));

    String byteArrayValueId = ((HistoricDetailVariableInstanceUpdateEntity) historyService.createHistoricDetailQuery().singleResult()).getByteArrayValueId();

    // when
    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired()
        .execute(new GetByteArrayCommand(byteArrayValueId));

    checkBinary(byteArrayEntity);
  }

  @Test
  public void testHistoricDecisionInputInstanceBinary() {
    testRule.deploy(DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN);

    startProcessInstanceAndEvaluateDecision(new JavaSerializable("foo"));

    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputInstances = historicDecisionInstance.getInputs();
    assertEquals(1, inputInstances.size());

    String byteArrayValueId = ((HistoricDecisionInputInstanceEntity) inputInstances.get(0)).getByteArrayValueId();

    // when
    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(byteArrayValueId));

    checkBinary(byteArrayEntity);
  }

  @Test
  public void testHistoricDecisionOutputInstanceBinary() {
    testRule.deploy(DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN);

    startProcessInstanceAndEvaluateDecision(new JavaSerializable("foo"));

    HistoricDecisionInstance historicDecisionInstance = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputInstances = historicDecisionInstance.getOutputs();
    assertEquals(1, outputInstances.size());


    String byteArrayValueId = ((HistoricDecisionOutputInstanceEntity) outputInstances.get(0)).getByteArrayValueId();

    // when
    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(byteArrayValueId));

    checkBinary(byteArrayEntity);
  }

  @Test
  public void testAttachmentContentBinaries() {
      // create and save task
      Task task = taskService.newTask();
      taskService.saveTask(task);
      taskId = task.getId();

      // when
      AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment("web page", taskId, "someprocessinstanceid", "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));

      ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(attachment.getContentId()));

      checkBinary(byteArrayEntity);
  }

  @Test
  public void testHistoricExceptionStacktraceBinary() {
    // given
    BpmnModelInstance instance = createFailingProcess();
    testRule.deploy(instance);
    runtimeService.startProcessInstanceByKey("Process");
    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    HistoricJobLogEventEntity entity = (HistoricJobLogEventEntity) historyService
        .createHistoricJobLogQuery()
        .failureLog()
        .singleResult();
    assertNotNull(entity);

    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(entity.getExceptionByteArrayId()));

    checkBinary(byteArrayEntity);
  }

  @Test
  public void testHistoricExternalTaskJobLogStacktraceBinary() {
    // given
    testRule.deploy("org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml");
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = tasks.get(0);

    // submitting a failure (after a simulated processing time of three seconds)
    ClockUtil.setCurrentTime(nowPlus(3000L));

    String errorMessage;
    String exceptionStackTrace;
    try {
      throw new RuntimeSqlException("test cause");
    } catch (RuntimeException e) {
      exceptionStackTrace = ExceptionUtils.getStackTrace(e);
      errorMessage = e.getMessage();
    }
    assertNotNull(exceptionStackTrace);

    externalTaskService.handleFailure(task.getId(), WORKER_ID, errorMessage, exceptionStackTrace, 5, 3000L);

    HistoricExternalTaskLogEntity entity = (HistoricExternalTaskLogEntity) historyService.createHistoricExternalTaskLogQuery().errorMessage(errorMessage).singleResult();
    assertNotNull(entity);

    ByteArrayEntity byteArrayEntity = configuration.getCommandExecutorTxRequired().execute(new GetByteArrayCommand(entity.getErrorDetailsByteArrayId()));

    // then
    checkBinary(byteArrayEntity);
  }

  protected void checkBinary(ByteArrayEntity byteArrayEntity) {
    assertNotNull(byteArrayEntity);
    assertNotNull(byteArrayEntity.getCreateTime());
    assertEquals(HISTORY.getValue(), byteArrayEntity.getType());
  }

  protected FileValue createFile() {
    String fileName = "text.txt";
    String encoding = "crazy-encoding";
    String mimeType = "martini/dry";

    FileValue fileValue = Variables
        .fileValue(fileName)
        .file("ABC".getBytes())
        .encoding(encoding)
        .mimeType(mimeType)
        .create();
    return fileValue;
  }

  protected BpmnModelInstance createProcess() {
    return Bpmn.createExecutableProcess("Process")
      .startEvent()
      .userTask("user")
      .endEvent()
      .done();
  }

  protected BpmnModelInstance createFailingProcess() {
    return Bpmn.createExecutableProcess("Process")
      .startEvent()
      .serviceTask("failing")
      .camundaAsyncAfter()
      .camundaAsyncBefore()
      .camundaClass(FailingDelegate.class)
      .endEvent()
      .done();
  }

  protected ProcessInstance startProcessInstanceAndEvaluateDecision(Object input) {
    return engineRule.getRuntimeService().startProcessInstanceByKey("testProcess",
        Variables.createVariables().putValue("input1", input));
  }


  protected Date nowPlus(long millis) {
    return new Date(ClockUtil.getCurrentTime().getTime() + millis);
  }
}
