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

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.performance.engine.junit.ProcessEngineJobExecutorPerformanceTestCase;
import org.camunda.bpm.qa.performance.engine.steps.CountJobsStep;
import org.camunda.bpm.qa.performance.engine.steps.StartProcessInstanceStep;
import org.camunda.bpm.qa.performance.engine.steps.WaitStep;
import org.junit.Test;

/**
 * @author: Johannes Heinemann
 */
public class MultiInstancePerformanceTest extends ProcessEngineJobExecutorPerformanceTestCase {

  @Test
  @Deployment(resources = {"org/camunda/bpm/qa/performance/engine/bpmn/MultiInstancePerformanceTest.oneAsyncServiceTask.bpmn",
      "org/camunda/bpm/qa/performance/engine/bpmn/MultiInstancePerformanceTest.subProcessWithAsyncCallActivity.bpmn"})
  public void subProcessWithAsyncCallActivity() {

    performanceTest()
        .step(new StartProcessInstanceStep(engine, "mainprocess"))
        .step(new WaitStep())
        .step(new CountJobsStep(engine))
        .run();
  }
}
