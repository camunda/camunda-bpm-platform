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
package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * This test makes sure that if the transaction synchronization / transaction listener ExclusiveJobAddedNotification is
 * executed in a different thread than the Thread which executed the job, the notification still works.
 *
 * See: https://app.camunda.com/jira/browse/CAM-3684
 *
 * @author Daniel Meyer
 *
 */
public class TransactionListenerThreadContextTest {

  @Rule
  public ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/engine/test/jobexecutor/TransactionListenerThreadContextTest.cfg.xml");
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
  }

  @Test
  public void testTxListenersInvokeAsync() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
        .camundaAsyncBefore()
        .camundaAsyncAfter()
      .endEvent()
      .done();

    Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("testProcess.bpmn", process)
      .deploy();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    testRule.waitForJobExecutorToProcessAllJobs(6000);


    testRule.assertProcessEnded(pi.getId());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

}
