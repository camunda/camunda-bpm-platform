/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.performance.engine.bpmn;

import static org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants.*;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.performance.engine.junit.ProcessEnginePerformanceTestCase;
import org.camunda.bpm.qa.performance.engine.steps.SignalExecutionStep;
import org.camunda.bpm.qa.performance.engine.steps.StartProcessInstanceStep;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class SequencePerformanceTest extends ProcessEnginePerformanceTestCase {

  @Test
  @Deployment
  public void syncSequence1Step() {
    perfomanceTest()
      .step(new StartProcessInstanceStep(engine, "process"))
    .run();
  }

  @Test
  @Deployment
  public void syncSequence5Steps() {
    perfomanceTest()
      .step(new StartProcessInstanceStep(engine, "process"))
    .run();
  }

  @Test
  @Deployment
  public void syncSequence15Steps() {
    perfomanceTest()
      .step(new StartProcessInstanceStep(engine, "process"))
    .run();
  }

  @Test
  @Deployment
  public void asyncSequence1Step() {
    perfomanceTest()
      .step(new StartProcessInstanceStep(engine, "process"))
      .step(new SignalExecutionStep(engine, PROCESS_INSTANCE_ID))
    .run();
  }

  @Test
  @Deployment
  public void asyncSequence5Steps() {
    perfomanceTest()
      .step(new StartProcessInstanceStep(engine, "process"))
      .steps(5, new SignalExecutionStep(engine, PROCESS_INSTANCE_ID))
    .run();
  }

  @Test
  @Deployment
  public void asyncSequence15Steps() {
    perfomanceTest()
      .step(new StartProcessInstanceStep(engine, "process"))
      .steps(15, new SignalExecutionStep(engine, PROCESS_INSTANCE_ID))
    .run();
  }



}
