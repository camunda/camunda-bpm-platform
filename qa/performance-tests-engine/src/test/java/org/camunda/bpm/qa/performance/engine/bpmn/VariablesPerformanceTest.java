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
package org.camunda.bpm.qa.performance.engine.bpmn;

import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE1;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE10;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE2;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE3;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE4;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE5;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE6;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE7;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE8;
import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.VARIABLE9;

import java.util.HashMap;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.performance.engine.junit.ProcessEnginePerformanceTestCase;
import org.camunda.bpm.qa.performance.engine.steps.StartProcessInstanceStep;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class VariablesPerformanceTest extends ProcessEnginePerformanceTestCase {


  @Test
  @Deployment(resources =
    {"org/camunda/bpm/qa/performance/engine/bpmn/StartEventPerformanceTest.noneStartEvent.bpmn"})
  public void noneStartEventStringVar() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE1, "someValue");

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();
  }

  @Test
  @Deployment(resources =
    {"org/camunda/bpm/qa/performance/engine/bpmn/StartEventPerformanceTest.noneStartEvent.bpmn"})
  public void noneStartEvent10StringVars() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE1, "someValue");
    variables.put(VARIABLE2, "someValue");
    variables.put(VARIABLE3, "someValue");
    variables.put(VARIABLE4, "someValue");
    variables.put(VARIABLE5, "someValue");
    variables.put(VARIABLE6, "someValue");
    variables.put(VARIABLE7, "someValue");
    variables.put(VARIABLE8, "someValue");
    variables.put(VARIABLE9, "someValue");
    variables.put(VARIABLE10, "someValue");

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();
  }

  @Test
  @Deployment(resources =
    {"org/camunda/bpm/qa/performance/engine/bpmn/StartEventPerformanceTest.noneStartEvent.bpmn"})
  public void noneStartEventStringVar2() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE1, "Some Text which is considerably longer than the first one.");

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();
  }

  @Test
  @Deployment(resources =
    {"org/camunda/bpm/qa/performance/engine/bpmn/StartEventPerformanceTest.noneStartEvent.bpmn"})
  public void noneStartEventDoubleVar() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE1, 2d);

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();
  }

  @Test
  @Deployment(resources =
    {"org/camunda/bpm/qa/performance/engine/bpmn/StartEventPerformanceTest.noneStartEvent.bpmn"})
  public void noneStartEventByteVar() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE1, "This string will be saved as a byte array.".getBytes());

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();
  }

  @Test
  @Deployment(resources =
    {"org/camunda/bpm/qa/performance/engine/bpmn/StartEventPerformanceTest.noneStartEvent.bpmn"})
  public void noneStartEvent10ByteVars() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE1, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE2, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE3, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE4, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE5, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE6, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE7, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE8, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE9, "This string will be saved as a byte array.".getBytes());
    variables.put(VARIABLE10, "This string will be saved as a byte array.".getBytes());

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();
  }

  @Test
  @Deployment(resources =
    {"org/camunda/bpm/qa/performance/engine/bpmn/StartEventPerformanceTest.noneStartEvent.bpmn"})
  public void noneStartEventLargeByteVar() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    byte[] bytes = new byte[5*1024];
    variables.put(VARIABLE1, bytes);

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();
  }

}
