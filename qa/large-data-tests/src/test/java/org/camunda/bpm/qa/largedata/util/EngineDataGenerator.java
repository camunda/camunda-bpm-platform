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
package org.camunda.bpm.qa.largedata.util;

import com.google.common.collect.Lists;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.camunda.bpm.qa.largedata.util.DmnHelper.createSimpleDmnModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EngineDataGenerator {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  protected static final String USER_ID = "testUser";
  protected static final String GROUP_ID = "testGroup";
  protected static final String USER_TASK_PROCESS_KEY = "userTaskProcess";
  protected static final String AUTO_COMPLETE_PROCESS_KEY = "autoCompleteProcess";
  protected static final String ASYNC_TASK_PROCESS_KEY = "asyncProcess";
  protected static final String DECISION_KEY = "simpleDecisionKey";
  protected static final String DEPLOYMENT_NAME = "testDeployment";

  protected final IdentityService identityService;
  protected final DecisionService decisionService;
  protected final RepositoryService repositoryService;
  protected final RuntimeService runtimeService;
  protected final TaskService taskService;
  protected final ProcessEngine processEngine;

  public final int numberOfInstancesToGenerate;
  public final String keyPrefix;

  // allows to configure how many commands are executed in one transaction if jdbc transaction is enabled
  public static final int BATCH_SIZE = 100;

  public EngineDataGenerator(final ProcessEngine processEngine, final int numberOfInstancesToGenerate, final String keyPrefix) {
    this.processEngine = processEngine;
    this.identityService = processEngine.getIdentityService();
    this.decisionService = processEngine.getDecisionService();
    this.repositoryService = processEngine.getRepositoryService();
    this.runtimeService = processEngine.getRuntimeService();
    this.taskService = processEngine.getTaskService();

    this.numberOfInstancesToGenerate = numberOfInstancesToGenerate;
    this.keyPrefix = keyPrefix;
  }

  public void generateData() {
    logger.info("Generating engine test data...");
    deployDefinitions();
    generateUserTaskData();
    generateCompletedProcessInstanceData();
    generateDecisionInstanceData();
    generateOpLogData();
    logger.info("Generation of engine test data has been completed.");
  }


  public void generateDecisionInstanceData() {
    logger.info("Generating decision instance data...");
    final List<Integer> sequenceNumberList = createSequenceNumberList();
    generateInBatches(
      sequenceNumberList,
      (ignored) -> evaluateDecision()
    );
    logger.info("Successfully generated decision instance data.");
  }

  protected void evaluateDecision() {
    decisionService
      .evaluateDecisionByKey(getDecisionKey())
      .variables(createSimpleVariables())
      .evaluate();
  }

  public void generateCompletedProcessInstanceData() {
    logger.info("Generating completed process instance data...");
    final List<Integer> sequenceNumberList = createSequenceNumberList();
    generateInBatches(
      sequenceNumberList,
      (ignored) -> startAutoCompleteProcess()
    );
    logger.info("Successfully generated completed process instance data...");
  }

  protected void startAutoCompleteProcess() {
    runtimeService.startProcessInstanceByKey(getAutoCompleteProcessKey(), createSimpleVariables());
  }

  public void generateAsyncTaskProcessInstanceData() {
    logger.info("Generating async task process instance data...");
    final List<Integer> sequenceNumberList = createSequenceNumberList();
    generateInBatches(
        sequenceNumberList,
        (ignored) -> startAsyncTaskProcess()
    );
    logger.info("Successfully generated async task process instance data...");
  }

  public void startAsyncTaskProcess() {
    runtimeService.startProcessInstanceByKey(getAsyncTaskProcessKey());
  }

  public void generateUserTaskData() {
    // the user task data includes data for tasks, identity link log
    logger.info("Generating user task data....");
    final List<Integer> sequenceNumberList = createSequenceNumberList();
    generateInBatches(
      sequenceNumberList,
      (ignored) -> startUserTaskProcess()
    );
    createUser();
    createGroup();
    setCandidateUserAndGroupForAllUserTask();
    completeAllUserTasks();
    logger.info("User task data successfully generated.");
  }
  
  public void generateOpLogData() {

    for (int i = 0; i < numberOfInstancesToGenerate; i++) {
      repositoryService.suspendProcessDefinitionByKey(getUserTaskProcessKey());
      repositoryService.activateProcessDefinitionByKey(getUserTaskProcessKey());
    }
  }

  public void deployDefinitions() {
    logger.info("Deploying process & decision definitions...");
    BpmnModelInstance userTaskProcessModelInstance = createUserTaskProcess();
    BpmnModelInstance autoCompleteProcessModelInstance = createSimpleServiceTaskProcess();
    BpmnModelInstance asyncTaskProcessModelInstance = createAsyncServiceTaskProcess();
    final DmnModelInstance decisionModelInstance = createSimpleDmnModel(getDecisionKey());
    DeploymentBuilder deploymentbuilder = repositoryService.createDeployment();
    deploymentbuilder.name(getDeploymentName());
    deploymentbuilder.addModelInstance("userTaskProcess.bpmn", userTaskProcessModelInstance);
    deploymentbuilder.addModelInstance("autoCompleteProcess.bpmn", autoCompleteProcessModelInstance);
    deploymentbuilder.addModelInstance("asyncTaskProcess.bpmn", asyncTaskProcessModelInstance);
    deploymentbuilder.addModelInstance("simpleDecision.dmn", decisionModelInstance);
    deploymentbuilder.deploy();
    logger.info("Definitions successfully deployed.");
  }

  public void createUser() {
    User user = identityService.newUser(getUserId());
    identityService.saveUser(user);
  }

  public void createGroup() {
    Group group = identityService.newGroup(getGroupId());
    identityService.saveGroup(group);
  }

  protected void setCandidateUserAndGroupForAllUserTask() {
    List<Task> list = taskService.createTaskQuery().list();
    identityService.setAuthenticatedUserId(getUserId());
    generateInBatches(
      list,
      (task) -> {
        taskService.addCandidateUser(task.getId(), getUserId());
        taskService.addCandidateGroup(task.getId(), getGroupId());
      }
    );
  }

  protected void completeAllUserTasks() {
    List<Task> list = taskService.createTaskQuery().list();
    generateInBatches(
      list,
      (task) -> {
        taskService.claim(task.getId(), getUserId());
        taskService.complete(task.getId());
      }
    );
  }

  protected void startUserTaskProcess() {
    runtimeService.startProcessInstanceByKey(getUserTaskProcessKey(), createSimpleVariables());
  }

  protected List<Integer> createSequenceNumberList() {
    return IntStream.range(0, numberOfInstancesToGenerate)
      .boxed().collect(Collectors.toList());
  }


  protected <T> void generateInBatches(List<T> allEntries, Consumer<T> generateData) {
    final List<List<T>> partition = Lists.partition(allEntries, BATCH_SIZE);
    partition.forEach(batch -> {
      ProcessEngineConfigurationImpl configuration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
      configuration.getCommandExecutorTxRequired().execute(
        (Command<Void>) commandContext -> {
          batch.forEach(generateData);
          return null;
        }
      );
    });
  }

  protected Map<String, Object> createSimpleVariables() {
    Random random = new Random();
    Map<String, Object> variables = new HashMap<>();
    int integer = random.nextInt();
    variables.put("stringVar", "aStringValue");
    variables.put("boolVar", random.nextBoolean());
    variables.put("integerVar", random.nextInt());
    variables.put("shortVar", (short) integer);
    variables.put("longVar", random.nextLong());
    variables.put("doubleVar", random.nextDouble());
    variables.put("dateVar", new Date(random.nextInt()));
    return variables;
  }

  protected String testSpecificKey(String key) {
    return keyPrefix + key;
  }

  protected BpmnModelInstance createSimpleServiceTaskProcess() {
    return Bpmn.createExecutableProcess(getAutoCompleteProcessKey())
        .camundaHistoryTimeToLive(180)
        .startEvent()
      .serviceTask()
        .camundaExpression("${true}")
      .endEvent()
      .done();
  }

  protected BpmnModelInstance createAsyncServiceTaskProcess() {
    return Bpmn.createExecutableProcess(getAsyncTaskProcessKey())
        .camundaHistoryTimeToLive(180)
        .startEvent()
        .serviceTask()
          .camundaAsyncBefore()
          .camundaExpression("${true}")
        .endEvent()
        .done();
  }

  protected BpmnModelInstance createUserTaskProcess() {
    return Bpmn.createExecutableProcess(getUserTaskProcessKey())
        .camundaHistoryTimeToLive(180)
        .startEvent()
      .userTask("userTaskToComplete")
      .userTask("pendingUserTask")
      .endEvent()
      .done();
  }

  public String getUserId() {
    return testSpecificKey(USER_ID);
  }

  public String getGroupId() {
    return testSpecificKey(GROUP_ID);
  }

  public String getUserTaskProcessKey() {
    return testSpecificKey(USER_TASK_PROCESS_KEY);
  }

  public String getAutoCompleteProcessKey() {
    return testSpecificKey(AUTO_COMPLETE_PROCESS_KEY);
  }

  public String getAsyncTaskProcessKey() {
    return testSpecificKey(ASYNC_TASK_PROCESS_KEY);
  }

  public String getDecisionKey() {
    return testSpecificKey(DECISION_KEY);
  }

  public String getDeploymentName() {
    return testSpecificKey(DEPLOYMENT_NAME);
  }
}
