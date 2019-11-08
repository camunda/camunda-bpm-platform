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
import org.camunda.bpm.qa.performance.engine.junit.ProcessEnginePerformanceTestCase;
import org.camunda.bpm.qa.performance.engine.steps.StartProcessInstanceStep;
import org.camunda.bpm.qa.performance.engine.steps.WorkExternalTaskStep;
import org.junit.Test;

/**
 * @author: Paul Lungu
 */
public class ExternalTaskPerformanceTest extends ProcessEnginePerformanceTestCase {
  
  @Test
  @Deployment(resources = {"org/camunda/bpm/qa/performance/engine/bpmn/NFT_TEST_FLOW_External_20.bpmn"})
  public void sequence20ExternalServiceTasks() {
	// Performance test external service task EXTERNAL_STEP_1
    performanceTest()
    .step(new StartProcessInstanceStep(engine, "NFT_TEST_FLOW_External_20"))
    .step(new WorkExternalTaskStep(engine, "STEP_1", "worker"+1, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_2", "worker"+2, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_3", "worker"+3, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_4", "worker"+4, 1))    
    .step(new WorkExternalTaskStep(engine, "STEP_5", "worker"+5, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_6", "worker"+6, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_7", "worker"+7, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_8", "worker"+8, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_9", "worker"+9, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_10", "worker"+10, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_11", "worker"+11, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_12", "worker"+12, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_13", "worker"+13, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_14", "worker"+14, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_15", "worker"+15, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_16", "worker"+16, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_17", "worker"+17, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_18", "worker"+18, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_19", "worker"+19, 1))
    .step(new WorkExternalTaskStep(engine, "STEP_20", "worker"+20, 1))
    .run();
  }
  
  
}
