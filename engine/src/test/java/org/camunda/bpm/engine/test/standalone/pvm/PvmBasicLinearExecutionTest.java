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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.test.standalone.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.standalone.pvm.activities.End;
import org.camunda.bpm.engine.test.standalone.pvm.activities.WaitState;
import org.camunda.bpm.engine.test.standalone.pvm.activities.While;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class PvmBasicLinearExecutionTest {

  /**
   * +-------+   +-----+
   * | start |-->| end |
   * +-------+   +-----+
   */
  @Test
  public void testStartEnd() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   * +-----+   +-----+   +-------+
   * | one |-->| two |-->| three |
   * +-----+   +-----+   +-------+
   */
  @Test
  public void testSingleAutomatic() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("one")
        .initial()
        .behavior(new Automatic())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   * +-----+   +-----+   +-------+
   * | one |-->| two |-->| three |
   * +-----+   +-----+   +-------+
   */
  @Test
  public void testSingleWaitState() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("one")
        .initial()
        .behavior(new Automatic())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new WaitState())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    PvmExecution activityInstance = processInstance.findExecution("two");
    assertNotNull(activityInstance);

    activityInstance.signal(null, null);

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   * +-----+   +-----+   +-------+   +------+    +------+
   * | one |-->| two |-->| three |-->| four |--> | five |
   * +-----+   +-----+   +-------+   +------+    +------+
   */
  @Test
  public void testCombinationOfWaitStatesAndAutomatics() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("one")
      .endActivity()
      .createActivity("one")
        .behavior(new WaitState())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new WaitState())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new Automatic())
        .transition("four")
      .endActivity()
      .createActivity("four")
        .behavior(new Automatic())
        .transition("five")
      .endActivity()
      .createActivity("five")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    PvmExecution activityInstance = processInstance.findExecution("one");
    assertNotNull(activityInstance);
    activityInstance.signal(null, null);

    activityInstance = processInstance.findExecution("two");
    assertNotNull(activityInstance);
    activityInstance.signal(null, null);

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   *                  +----------------------------+
   *                  v                            |
   * +-------+   +------+   +-----+   +-----+    +-------+
   * | start |-->| loop |-->| one |-->| two |--> | three |
   * +-------+   +------+   +-----+   +-----+    +-------+
   *                  |
   *                  |   +-----+
   *                  +-->| end |
   *                      +-----+
   */
  @Test
  public void testWhileLoop() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("loop")
      .endActivity()
      .createActivity("loop")
        .behavior(new While("count", 0, 10))
        .transition("one", "more")
        .transition("end", "done")
      .endActivity()
      .createActivity("one")
        .behavior(new Automatic())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new Automatic())
        .transition("loop")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

}
