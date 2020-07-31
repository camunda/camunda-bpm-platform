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

import java.util.ArrayList;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper.ThreadControl;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class ReuseEntityCacheTest {

  public static final String ENTITY_ID1 = "Execution1";
  public static final String ENTITY_ID2 = "Execution2";

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJobExecutor(new ControllableJobExecutor()));
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);

  protected boolean defaultSetting;

  protected ControllableJobExecutor jobExecutor;

  protected static ThreadControl executionThreadControl;
  protected ThreadControl acquisitionThreadControl;

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask()
        .camundaClass(CreateEntitiesDelegate.class.getName())
        .camundaAsyncBefore()
        .camundaExclusive(true)
      .serviceTask().camundaClass(UpdateEntitiesDelegate.class.getName())
        .camundaAsyncBefore()
        .camundaExclusive(true)
      .serviceTask().camundaClass(RemoveEntitiesDelegate.class.getName())
        .camundaAsyncBefore()
        .camundaExclusive(true)
      .endEvent()
      .done();

  @Before
  public void setUp() {
    defaultSetting = getEngineConfig().isDbEntityCacheReuseEnabled();
    getEngineConfig().setDbEntityCacheReuseEnabled(true);
    jobExecutor = (ControllableJobExecutor) getEngineConfig().getJobExecutor();
    executionThreadControl = jobExecutor.getExecutionThreadControl();
    acquisitionThreadControl = jobExecutor.getAcquisitionThreadControl();
  }

  @After
  public void resetEngineConfiguration() {
    getEngineConfig().setDbEntityCacheReuseEnabled(defaultSetting);
  }

  @After
  public void shutdownJobExecutor() {
    jobExecutor.shutdown();
  }

  @Test
  public void testFlushOrderWithEntityCacheReuse() {
    // given
    Deployment deployment = engineRule
        .getRepositoryService()
        .createDeployment()
        .addModelInstance("foo.bpmn", PROCESS)
        .deploy();
    engineRule.manageDeployment(deployment);

    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    jobExecutor.start();

    // the job is acquired
    acquisitionThreadControl.waitForSync();

    // and job acquisition finishes successfully
    acquisitionThreadControl.makeContinueAndWaitForSync();
    acquisitionThreadControl.makeContinue();

    // and the first delegate is completed
    executionThreadControl.waitForSync();
    executionThreadControl.makeContinueAndWaitForSync();

    // and the second delegate is completed
    executionThreadControl.makeContinueAndWaitForSync();

    // and the third delegate is completed
    executionThreadControl.makeContinue();

    acquisitionThreadControl.waitForSync();

    // then the job has been successfully executed
    Assert.assertEquals(0, engineRule.getManagementService().createJobQuery().count());
  }

  protected ProcessEngineConfigurationImpl getEngineConfig() {
    return (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();
  }

  public static class CreateEntitiesDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      ExecutionEntity execution1 = new ExecutionEntity();
      execution1.setId(ENTITY_ID1);
      execution1.setExecutions(new ArrayList<ExecutionEntity>());

      ExecutionEntity execution2 = new ExecutionEntity();
      execution2.setId(ENTITY_ID2);
      execution2.setExecutions(new ArrayList<ExecutionEntity>());
      execution2.setParent(execution1);

      ExecutionManager executionManager = Context.getCommandContext().getExecutionManager();
      executionManager.insert(execution1);
      executionManager.insert(execution2);

      executionThreadControl.sync();

    }

  }

  public static class UpdateEntitiesDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      ExecutionManager executionManager = Context.getCommandContext().getExecutionManager();
      ExecutionEntity execution1 = executionManager.findExecutionById(ENTITY_ID1);
      ExecutionEntity execution2 = executionManager.findExecutionById(ENTITY_ID2);

      // revert the references
      execution2.setParent(null);
      execution1.setParent(execution2);

      executionThreadControl.sync();

    }

  }

  public static class RemoveEntitiesDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      ExecutionManager executionManager = Context.getCommandContext().getExecutionManager();
      ExecutionEntity execution1 = executionManager.findExecutionById(ENTITY_ID1);
      ExecutionEntity execution2 = executionManager.findExecutionById(ENTITY_ID2);

      executionManager.delete(execution1);
      executionManager.delete(execution2);

      executionThreadControl.sync();

    }

  }
}
