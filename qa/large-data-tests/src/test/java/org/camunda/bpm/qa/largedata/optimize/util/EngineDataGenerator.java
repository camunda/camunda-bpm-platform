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
package org.camunda.bpm.qa.largedata.optimize.util;

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
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.camunda.bpm.qa.largedata.optimize.util.DmnHelper.createSimpleDmnModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EngineDataGenerator {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private static final String userId = "testUser";
  private static final String groupId = "testGroup";
  private static final String USER_TASK_PROCESS_KEY = "userTaskProcess";
  private static final String AUTO_COMPLETE_PROCESS_KEY = "autoCompleteProcess";
  private static final String DECISION_KEY = "simpleDecisionKey";

  private final IdentityService identityService;
  private final DecisionService decisionService;
  private final RepositoryService repositoryService;
  private final RuntimeService runtimeService;
  private final TaskService taskService;
  private final ProcessEngine processEngine;

  public final int numberOfInstancesToGenerate;

  // allows to configure how many commands are executed in one transaction if jdbc transaction is enabled
  public static final int BATCH_SIZE = 100;

  public EngineDataGenerator(final ProcessEngine processEngine, final int optimizePageSize) {
    this.processEngine = processEngine;
    this.identityService = processEngine.getIdentityService();
    this.decisionService = processEngine.getDecisionService();
    this.repositoryService = processEngine.getRepositoryService();
    this.runtimeService = processEngine.getRuntimeService();
    this.taskService = processEngine.getTaskService();

    // we double the amount of instances to generate to make sure that there are at least two pages
    // of each entity available
    numberOfInstancesToGenerate = optimizePageSize * 2;
  }

  public void generateData() {
    logger.info("Generating engine data for Optimize rest tests...");
    deployDefinitions();
    generateUserTaskData();
    generateCompletedProcessInstanceData();
    generateDecisionInstanceData();
    generateOpLogData();
    logger.info("Generation of engine data has been completed.");
  }


  private void generateDecisionInstanceData() {
    logger.info("Generating decision instance data...");
    final List<Integer> sequenceNumberList = createSequenceNumberList();
    generateInBatches(
      sequenceNumberList,
      (ignored) -> evaluateDecision()
    );
    logger.info("Successfully generated decision instance data.");
  }

  private void evaluateDecision() {
    decisionService
      .evaluateDecisionByKey(DECISION_KEY)
      .variables(createSimpleVariables())
      .evaluate();
  }

  private void generateCompletedProcessInstanceData() {
    logger.info("Generating completed process instance data...");
    final List<Integer> sequenceNumberList = createSequenceNumberList();
    generateInBatches(
      sequenceNumberList,
      (ignored) -> startAutoCompleteProcess()
    );
    logger.info("Successfully generated completed process instance data...");
  }

  private void startAutoCompleteProcess() {
    runtimeService.startProcessInstanceByKey(AUTO_COMPLETE_PROCESS_KEY, createSimpleVariables());
  }

  private void generateUserTaskData() {
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
  
  private void generateOpLogData() {

    for (int i = 0; i < numberOfInstancesToGenerate; i++) {
      repositoryService.suspendProcessDefinitionByKey(USER_TASK_PROCESS_KEY);
      repositoryService.activateProcessDefinitionByKey(USER_TASK_PROCESS_KEY);
    }
  }

  private void deployDefinitions() {
    logger.info("Deploying process & decision definitions...");
    BpmnModelInstance userTaskProcessModelInstance = createUserTaskProcess();
    BpmnModelInstance autoCompleteProcessModelInstance = createSimpleServiceTaskProcess();
    final DmnModelInstance decisionModelInstance = createSimpleDmnModel(DECISION_KEY);
    DeploymentBuilder deploymentbuilder = repositoryService.createDeployment();
    deploymentbuilder.addModelInstance("userTaskProcess.bpmn", userTaskProcessModelInstance);
    deploymentbuilder.addModelInstance("autoCompleteProcess.bpmn", autoCompleteProcessModelInstance);
    deploymentbuilder.addModelInstance("simpleDecision.dmn", decisionModelInstance);
    deploymentbuilder.deploy();
    logger.info("Definitions successfully deployed.");
  }

  private void createUser() {
    User user = identityService.newUser(EngineDataGenerator.userId);
    identityService.saveUser(user);
  }

  private void createGroup() {
    Group group = identityService.newGroup(groupId);
    identityService.saveGroup(group);
  }

  private void setCandidateUserAndGroupForAllUserTask() {
    List<Task> list = taskService.createTaskQuery().list();
    identityService.setAuthenticatedUserId(userId);
    generateInBatches(
      list,
      (task) -> {
        taskService.addCandidateUser(task.getId(), userId);
        taskService.addCandidateGroup(task.getId(), groupId);
      }
    );
  }

  private void completeAllUserTasks() {
    List<Task> list = taskService.createTaskQuery().list();
    generateInBatches(
      list,
      (task) -> {
        taskService.claim(task.getId(), userId);
        taskService.complete(task.getId());
      }
    );
  }

  private void startUserTaskProcess() {
    runtimeService.startProcessInstanceByKey(USER_TASK_PROCESS_KEY, createSimpleVariables());
  }

  private List<Integer> createSequenceNumberList() {
    return IntStream.range(0, numberOfInstancesToGenerate)
      .boxed().collect(Collectors.toList());
  }


  private <T> void generateInBatches(List<T> allEntries, Consumer<T> generateData) {
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

  private Map<String, Object> createSimpleVariables() {
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

  private BpmnModelInstance createSimpleServiceTaskProcess() {
    return Bpmn.createExecutableProcess(AUTO_COMPLETE_PROCESS_KEY)
      .startEvent()
      .serviceTask()
        .camundaExpression("${true}")
      .endEvent()
      .done();
  }

  private static BpmnModelInstance createUserTaskProcess() {
    return Bpmn.createExecutableProcess(USER_TASK_PROCESS_KEY)
      .startEvent()
      .userTask("userTaskToComplete")
      .userTask("pendingUserTask")
      .endEvent()
      .done();
  }
}
