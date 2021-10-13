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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.camunda.bpm.engine.test.standalone.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.standalone.pvm.activities.EmbeddedSubProcess;
import org.camunda.bpm.engine.test.standalone.pvm.activities.End;
import org.camunda.bpm.engine.test.standalone.pvm.activities.ParallelGateway;
import org.camunda.bpm.engine.test.standalone.pvm.activities.WaitState;
import org.camunda.bpm.engine.test.standalone.pvm.activities.While;
import org.camunda.bpm.engine.test.standalone.pvm.verification.TransitionInstanceVerifyer;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class PvmActivityInstanceTest {

  /**
   * +-----+   +-----+   +-------+
   * | one |-->| two |-->| three |
   * +-----+   +-----+   +-------+
   */
  @Test
  public void testSequence() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("one")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    verifier.assertStartInstanceCount(1, "one");
    verifier.assertStartInstanceCount(1, "two");
    verifier.assertStartInstanceCount(1, "three");

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

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();
    TransitionInstanceVerifyer transitionVerifier = new TransitionInstanceVerifyer();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .startTransition("loop")
          .executionListener(ExecutionListener.EVENTNAME_TAKE, transitionVerifier)
        .endTransition()
      .endActivity()
      .createActivity("loop")
        .behavior(new While("count", 0, 10))
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .startTransition("one", "more")
         .executionListener(ExecutionListener.EVENTNAME_TAKE, transitionVerifier)
        .endTransition()
        .startTransition("end", "done")
         .executionListener(ExecutionListener.EVENTNAME_TAKE, transitionVerifier)
        .endTransition()
      .endActivity()
      .createActivity("one")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("loop")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());

    verifier.assertStartInstanceCount(1, "start");
    verifier.assertProcessInstanceParent("start", processInstance);

    verifier.assertStartInstanceCount(11, "loop");
    verifier.assertProcessInstanceParent("loop", processInstance);

    verifier.assertStartInstanceCount(10, "one");
    verifier.assertProcessInstanceParent("one", processInstance);

    verifier.assertStartInstanceCount(10, "two");
    verifier.assertProcessInstanceParent("two", processInstance);

    verifier.assertStartInstanceCount(10, "three");
    verifier.assertProcessInstanceParent("three", processInstance);

    verifier.assertStartInstanceCount(1, "end");
    verifier.assertProcessInstanceParent("end", processInstance);
  }


  /**
   *           +-------------------------------------------------+
   *           | embeddedsubprocess        +----------+          |
   *           |                     +---->|endInside1|          |
   *           |                     |     +----------+          |
   *           |                     |                           |
   * +-----+   |  +-----------+   +----+   +----+   +----------+ |   +---+
   * |start|-->|  |startInside|-->|fork|-->|wait|-->|endInside2| |-->|end|
   * +-----+   |  +-----------+   +----+   +----+   +----------+ |   +---+
   *           |                     |                           |
   *           |                     |     +----------+          |
   *           |                     +---->|endInside3|          |
   *           |                           +----------+          |
   *           +-------------------------------------------------+
   */
  @Test
  public void testMultipleConcurrentEndsInsideEmbeddedSubProcessWithWaitState() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("fork")
        .endActivity()
        .createActivity("fork")
          .behavior(new ParallelGateway())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("endInside1")
          .transition("wait")
          .transition("endInside3")
        .endActivity()
        .createActivity("endInside1")
          .behavior(new End())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
        .createActivity("wait")
          .behavior(new WaitState())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("endInside2")
        .endActivity()
        .createActivity("endInside2")
          .behavior(new End())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
        .createActivity("endInside3")
          .behavior(new End())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertFalse(processInstance.isEnded());
    PvmExecution execution = processInstance.findExecution("wait");
    execution.signal(null, null);

    assertTrue(processInstance.isEnded());

    verifier.assertStartInstanceCount(1, "start");
    verifier.assertProcessInstanceParent("start", processInstance);

    verifier.assertStartInstanceCount(1, "embeddedsubprocess");
    verifier.assertProcessInstanceParent("embeddedsubprocess", processInstance);

    verifier.assertStartInstanceCount(1, "startInside");
    verifier.assertParent("startInside", "embeddedsubprocess");

    verifier.assertStartInstanceCount(1, "fork");
    verifier.assertParent("fork", "embeddedsubprocess");

    verifier.assertStartInstanceCount(1, "wait");
    verifier.assertParent("wait", "embeddedsubprocess");

    verifier.assertStartInstanceCount(1, "endInside1");
    verifier.assertParent("endInside1", "embeddedsubprocess");

    verifier.assertStartInstanceCount(1, "endInside2");
    verifier.assertParent("endInside2", "embeddedsubprocess");

    verifier.assertStartInstanceCount(1, "endInside3");
    verifier.assertParent("endInside3", "embeddedsubprocess");

    verifier.assertStartInstanceCount(1, "end");
    verifier.assertProcessInstanceParent("end", processInstance);

  }

  /**
   *           +-------------------------------------------------------+
   *           | embedded subprocess                                   |
   *           |                  +--------------------------------+   |
   *           |                  | nested embedded subprocess     |   |
   * +-----+   | +-----------+    |  +-----------+   +---------+   |   |   +---+
   * |start|-->| |startInside|--> |  |startInside|-->|endInside|   |   |-->|end|
   * +-----+   | +-----------+    |  +-----------+   +---------+   |   |   +---+
   *           |                  +--------------------------------+   |
   *           |                                                       |
   *           +-------------------------------------------------------+
   */
  @Test
  public void testNestedSubProcessNoEnd() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("nestedSubProcess")
        .endActivity()
          .createActivity("nestedSubProcess")
          .scope()
          .behavior(new EmbeddedSubProcess())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .createActivity("startNestedInside")
            .behavior(new Automatic())
            .executionListener(ExecutionListener.EVENTNAME_START, verifier)
            .executionListener(ExecutionListener.EVENTNAME_END, verifier)
            .transition("endInside")
            .endActivity()
          .createActivity("endInside")
            .behavior(new End())
            .executionListener(ExecutionListener.EVENTNAME_START, verifier)
            .executionListener(ExecutionListener.EVENTNAME_END, verifier)
            .endActivity()
        .endActivity()
      .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
         .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    assertTrue(processInstance.isEnded());

    verifier.assertStartInstanceCount(1, "start");
    verifier.assertProcessInstanceParent("start", processInstance);
    verifier.assertStartInstanceCount(1, "embeddedsubprocess");
    verifier.assertProcessInstanceParent("embeddedsubprocess", processInstance);
    verifier.assertStartInstanceCount(1, "startInside");
    verifier.assertParent("startInside", "embeddedsubprocess");
    verifier.assertStartInstanceCount(1, "nestedSubProcess");
    verifier.assertParent("nestedSubProcess", "embeddedsubprocess");
    verifier.assertStartInstanceCount(1, "startNestedInside");
    verifier.assertParent("startNestedInside", "nestedSubProcess");
    verifier.assertStartInstanceCount(1, "endInside");
    verifier.assertParent("endInside", "nestedSubProcess");
    verifier.assertStartInstanceCount(1, "end");
    verifier.assertProcessInstanceParent("end", processInstance);
  }

  /**
   *           +-------------------------------------------------------+
   *           | embedded subprocess                                   |
   *           |                  +--------------------------------+   |
   *           |                  | nested embedded subprocess     |   |
   * +-----+   | +-----------+    |  +-----------+                 |   |
   * |start|-->| |startInside|--> |  |startInside|                 |   |
   * +-----+   | +-----------+    |  +-----------+                 |   |
   *           |                  +--------------------------------+   |
   *           |                                                       |
   *           +-------------------------------------------------------+
   */
  @Test
  public void testNestedSubProcessBothNoEnd() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("nestedSubProcess")
        .endActivity()
          .createActivity("nestedSubProcess")
          .scope()
          .behavior(new EmbeddedSubProcess())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .createActivity("startNestedInside")
            .behavior(new Automatic())
            .executionListener(ExecutionListener.EVENTNAME_START, verifier)
            .executionListener(ExecutionListener.EVENTNAME_END, verifier)
            .endActivity()
        .endActivity()
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertTrue(processInstance.isEnded());

    verifier.assertStartInstanceCount(1, "start");
    verifier.assertProcessInstanceParent("start", processInstance);
    verifier.assertStartInstanceCount(1, "embeddedsubprocess");
    verifier.assertProcessInstanceParent("embeddedsubprocess", processInstance);
    verifier.assertStartInstanceCount(1, "startInside");
    verifier.assertParent("startInside", "embeddedsubprocess");
    verifier.assertStartInstanceCount(1, "nestedSubProcess");
    verifier.assertParent("nestedSubProcess", "embeddedsubprocess");
    verifier.assertStartInstanceCount(1, "startNestedInside");
    verifier.assertParent("startNestedInside", "nestedSubProcess");

  }

  @Test
  public void testSubProcessNoEnd() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
      .endActivity()
      .executionListener(ExecutionListener.EVENTNAME_START, verifier)
      .executionListener(ExecutionListener.EVENTNAME_END, verifier)
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertTrue(processInstance.isEnded());

    verifier.assertStartInstanceCount(1, "start");
    verifier.assertProcessInstanceParent("start", processInstance);
    verifier.assertStartInstanceCount(1, "embeddedsubprocess");
    verifier.assertProcessInstanceParent("embeddedsubprocess", processInstance);
    verifier.assertStartInstanceCount(1, "startInside");
    verifier.assertStartInstanceCount(1, "startInside");

  }


  /**
   * +-----+   +-----+   +-------+
   * | one |-->| two |-->| three |
   * +-----+   +-----+   +-------+
   */
  @Test
  public void testScopeActivity() {

    ActivityInstanceVerification verifier = new ActivityInstanceVerification();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("one")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("two")
      .endActivity()
      .createActivity("two")
        .scope()
        .behavior(new WaitState())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    PvmExecution childExecution = processInstance.findExecution("two");
    String parentActivityInstanceId = ((ExecutionImpl)childExecution).getParentActivityInstanceId();
    assertEquals(((ExecutionImpl)processInstance).getId(), parentActivityInstanceId);

    childExecution.signal(null, null);

    verifier.assertStartInstanceCount(1, "one");
    verifier.assertStartInstanceCount(1, "two");
    verifier.assertProcessInstanceParent("two", processInstance);
    verifier.assertStartInstanceCount(1, "three");

  }
}
