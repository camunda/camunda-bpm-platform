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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.test.standalone.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.standalone.pvm.activities.EmbeddedSubProcess;
import org.camunda.bpm.engine.test.standalone.pvm.activities.End;
import org.camunda.bpm.engine.test.standalone.pvm.activities.ParallelGateway;
import org.camunda.bpm.engine.test.standalone.pvm.activities.WaitState;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class PvmEventTest {

  /**
   * +-------+   +-----+
   * | start |-->| end |
   * +-------+   +-----+
   */
  @Test
  public void testStartEndEvents() {
    EventCollector eventCollector = new EventCollector();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("events")
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .startTransition("end")
          .executionListener(eventCollector)
        .endTransition()
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on ProcessDefinition(events)");
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("take on (start)-->(end)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on ProcessDefinition(events)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
  }

  /**
   *           +------------------------------+
   * +-----+   | +-----------+   +----------+ |   +---+
   * |start|-->| |startInside|-->|endInsdide| |-->|end|
   * +-----+   | +-----------+   +----------+ |   +---+
   *           +------------------------------+
   */
  @Test
  public void testEmbeddedSubProcessEvents() {
    EventCollector eventCollector = new EventCollector();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("events")
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
          .transition("endInside")
        .endActivity()
        .createActivity("endInside")
          .behavior(new End())
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on ProcessDefinition(events)");
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(embeddedsubprocess)");
    expectedEvents.add("start on Activity(startInside)");
    expectedEvents.add("end on Activity(startInside)");
    expectedEvents.add("start on Activity(endInside)");
    expectedEvents.add("end on Activity(endInside)");
    expectedEvents.add("end on Activity(embeddedsubprocess)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on ProcessDefinition(events)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
  }


  /**
   *                   +--+
   *              +--->|c1|---+
   *              |    +--+   |
   *              |           v
   * +-----+   +----+       +----+   +---+
   * |start|-->|fork|       |join|-->|end|
   * +-----+   +----+       +----+   +---+
   *              |           ^
   *              |    +--+   |
   *              +--->|c2|---+
   *                   +--+
   */
  @Test
  public void testSimpleAutmaticConcurrencyEvents() {
    EventCollector eventCollector = new EventCollector();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("events")
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on ProcessDefinition(events)");
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(fork)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(c2)");
    expectedEvents.add("end on Activity(c2)");
    expectedEvents.add("start on Activity(join)");
    expectedEvents.add("start on Activity(c1)");
    expectedEvents.add("end on Activity(c1)");
    expectedEvents.add("start on Activity(join)");
    expectedEvents.add("end on Activity(join)");
    expectedEvents.add("end on Activity(join)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on ProcessDefinition(events)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
  }

  /**
   *           +-----------------------------------------------+
   * +-----+   | +-----------+   +------------+   +----------+ |   +---+
   * |start|-->| |startInside|-->| taskInside |-->|endInsdide| |-->|end|
   * +-----+   | +-----------+   +------------+   +----------+ |   +---+
   *           +-----------------------------------------------+
   */
  @Test
  public void testEmbeddedSubProcessEventsDelete() {
    EventCollector eventCollector = new EventCollector();

    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("events")
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
      .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
          .transition("taskInside")
        .endActivity()
        .createActivity("taskInside")
          .behavior(new WaitState())
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
          .transition("endInside")
        .endActivity()
        .createActivity("endInside")
          .behavior(new End())
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
          .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    processInstance.deleteCascade("");

    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on ProcessDefinition(events)");
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(embeddedsubprocess)");
    expectedEvents.add("start on Activity(startInside)");
    expectedEvents.add("end on Activity(startInside)");
    expectedEvents.add("start on Activity(taskInside)");
    expectedEvents.add("end on Activity(taskInside)");
    expectedEvents.add("end on Activity(embeddedsubprocess)");
    expectedEvents.add("end on ProcessDefinition(events)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
  }
}
