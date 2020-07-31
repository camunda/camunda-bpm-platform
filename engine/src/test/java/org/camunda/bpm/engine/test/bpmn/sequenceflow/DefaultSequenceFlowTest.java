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
package org.camunda.bpm.engine.test.bpmn.sequenceflow;

import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.gateway.ExclusiveGatewayTest;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;


/**
 * See {@link ExclusiveGatewayTest} for a default sequence flow test on an exclusive gateway.
 * 
 * @author Joram Barrez
 */
public class DefaultSequenceFlowTest extends PluggableProcessEngineTest {
  
  @Deployment
  @Test
  public void testDefaultSequenceFlowOnTask() {
    String procId = runtimeService.startProcessInstanceByKey("defaultSeqFlow",
            CollectionUtil.singletonMap("input", 2)).getId();
    assertNotNull(runtimeService.createExecutionQuery().processInstanceId(procId).activityId("task2").singleResult());
    
    procId = runtimeService.startProcessInstanceByKey("defaultSeqFlow",
            CollectionUtil.singletonMap("input", 3)).getId();
    assertNotNull(runtimeService.createExecutionQuery().processInstanceId(procId).activityId("task3").singleResult());
    
    procId = runtimeService.startProcessInstanceByKey("defaultSeqFlow",
            CollectionUtil.singletonMap("input", 123)).getId();
    assertNotNull(runtimeService.createExecutionQuery().processInstanceId(procId).activityId("task1").singleResult());
  }

}
