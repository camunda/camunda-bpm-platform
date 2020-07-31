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
package org.camunda.bpm.engine.test.standalone.entity;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.util.BitMaskUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class ExecutionCachedEntityStateTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testProcessInstanceTasks() {
    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.TASKS_STATE_BIT), processInstance.getCachedEntityStateRaw());
  }

  @Deployment
  @Test
  public void testExecutionTasksScope() {
    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("userTask").singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.TASKS_STATE_BIT), execution.getCachedEntityStateRaw());

  }

  @Deployment
  @Test
  public void testExecutionTasksParallel() {
    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("userTask").singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.TASKS_STATE_BIT), execution.getCachedEntityStateRaw());

  }

  @Deployment
  @Test
  public void testExecutionTasksMi() {
    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    List<Execution> executions =  runtimeService.createExecutionQuery().activityId("userTask").list();
    for (Execution execution : executions) {
      int cachedEntityStateRaw = ((ExecutionEntity) execution).getCachedEntityStateRaw();
      if(!((ExecutionEntity)execution).isScope()) {
        assertEquals(
            BitMaskUtil.getMaskForBit(ExecutionEntity.TASKS_STATE_BIT)
            | BitMaskUtil.getMaskForBit(ExecutionEntity.VARIABLES_STATE_BIT)
            , cachedEntityStateRaw);
      } else {
        assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.VARIABLES_STATE_BIT), cachedEntityStateRaw);
      }
    }

  }

  @Deployment
  @Test
  public void testProcessInstanceEventSubscriptions() {
    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.EVENT_SUBSCRIPTIONS_STATE_BIT), processInstance.getCachedEntityStateRaw());
  }

  @Deployment
  @Test
  public void testExecutionEventSubscriptionsScope() {

    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("IntermediateCatchEvent_1").singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.EVENT_SUBSCRIPTIONS_STATE_BIT), execution.getCachedEntityStateRaw());

  }

  @Deployment
  @Test
  public void testExecutionEventSubscriptionsMi() {

    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    List<Execution> executions =  runtimeService.createExecutionQuery().activityId("ReceiveTask_1").list();
    for (Execution execution : executions) {
      int cachedEntityStateRaw = ((ExecutionEntity) execution).getCachedEntityStateRaw();

      if(!((ExecutionEntity)execution).isScope()) {
        assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.VARIABLES_STATE_BIT), cachedEntityStateRaw);
      } else {
        assertEquals(
            BitMaskUtil.getMaskForBit(ExecutionEntity.EVENT_SUBSCRIPTIONS_STATE_BIT), cachedEntityStateRaw);
      }
    }

  }

  @Deployment
  @Test
  public void testProcessInstanceJobs() {

    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.JOBS_STATE_BIT), processInstance.getCachedEntityStateRaw());

  }

  @Deployment
  @Test
  public void testExecutionJobsScope() {
    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("userTask").singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.JOBS_STATE_BIT), execution.getCachedEntityStateRaw());
  }

  @Deployment
  @Test
  public void testExecutionJobsParallel() {

    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("userTask").singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.JOBS_STATE_BIT), execution.getCachedEntityStateRaw());

  }

  @Deployment
  @Test
  public void testProcessInstanceIncident() {

    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    final ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("task").singleResult();
    assertEquals(0, execution.getCachedEntityStateRaw());

    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {

          IncidentContext incidentContext = new IncidentContext();
          incidentContext.setExecutionId(execution.getId());

          IncidentEntity.createAndInsertIncident("foo", incidentContext, null);

          return null;
        }
      });

    ExecutionEntity execution2 = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("task").singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.INCIDENT_STATE_BIT), execution2.getCachedEntityStateRaw());

  }

  @Deployment
  @Test
  public void testExecutionIncidentParallel() {

    runtimeService.startProcessInstanceByKey("testProcess");

    ExecutionEntity processInstance = (ExecutionEntity) runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals(0, processInstance.getCachedEntityStateRaw());

    final ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("task").singleResult();
    assertEquals(0, execution.getCachedEntityStateRaw());

    processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {

          IncidentContext incidentContext = new IncidentContext();
          incidentContext.setExecutionId(execution.getId());

          IncidentEntity.createAndInsertIncident("foo", incidentContext, null);

          return null;
        }
      });

    ExecutionEntity execution2 = (ExecutionEntity) runtimeService.createExecutionQuery().activityId("task").singleResult();
    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.INCIDENT_STATE_BIT), execution2.getCachedEntityStateRaw());

  }

  @Deployment
  @Test
  public void testExecutionExternalTask() {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    ExecutionEntity execution = (ExecutionEntity) runtimeService
        .createExecutionQuery()
        .activityId("externalTask")
        .singleResult();

    assertEquals(BitMaskUtil.getMaskForBit(ExecutionEntity.EXTERNAL_TASKS_BIT), execution.getCachedEntityStateRaw());

  }

}
