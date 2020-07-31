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
package org.camunda.bpm.engine.test.standalone.pvm;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.camunda.bpm.engine.test.standalone.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.standalone.pvm.activities.End;
import org.camunda.bpm.engine.test.standalone.pvm.activities.ParallelGateway;
import org.camunda.bpm.engine.test.standalone.pvm.activities.WaitState;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class PvmParallelTest {

  @Test
  public void testSimpleAutmaticConcurrency() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertTrue(processInstance.isEnded());
  }

  @Test
  public void testSimpleWaitStateConcurrency() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new WaitState())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new WaitState())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    PvmExecution activityInstanceC1 = processInstance.findExecution("c1");
    assertNotNull(activityInstanceC1);

    PvmExecution activityInstanceC2 = processInstance.findExecution("c2");
    assertNotNull(activityInstanceC2);

    activityInstanceC1.signal(null, null);
    activityInstanceC2.signal(null, null);

    List<String> activityNames = processInstance.findActiveActivityIds();
    List<String> expectedActivityNames = new ArrayList<String>();
    expectedActivityNames.add("end");

    assertEquals(expectedActivityNames, activityNames);
  }

  @Test
  public void testUnstructuredConcurrencyTwoJoins() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
        .transition("c3")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c3")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join1")
        .behavior(new ParallelGateway())
        .transition("c4")
      .endActivity()
      .createActivity("c4")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join2")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertNotNull(processInstance.findExecution("end"));
  }

  @Test
  public void testUnstructuredConcurrencyTwoForks() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork1")
      .endActivity()
      .createActivity("fork1")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
        .transition("fork2")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("fork2")
        .behavior(new ParallelGateway())
        .transition("c3")
        .transition("c4")
      .endActivity()
      .createActivity("c3")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("c4")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertNotNull(processInstance.findExecution("end"));
  }

  @Test
  public void testJoinForkCombinedInOneParallelGateway() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
        .transition("c3")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c3")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join1")
        .behavior(new ParallelGateway())
        .transition("c4")
        .transition("c5")
        .transition("c6")
      .endActivity()
      .createActivity("c4")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("c5")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("c6")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join2")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertNotNull(processInstance.findExecution("end"));
  }
}
