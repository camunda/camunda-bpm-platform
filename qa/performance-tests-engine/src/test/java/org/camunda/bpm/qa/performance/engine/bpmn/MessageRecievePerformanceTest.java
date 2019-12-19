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
import org.camunda.bpm.qa.performance.engine.steps.CorrelateMessageStep;
import org.camunda.bpm.qa.performance.engine.steps.PerfTestConstants;
import org.camunda.bpm.qa.performance.engine.steps.StartProcessInstanceStep;
import org.junit.Test;

public class MessageRecievePerformanceTest extends ProcessEnginePerformanceTestCase {
  
  @Test
  @Deployment(resources = {"org/camunda/bpm/qa/performance/engine/bpmn/NFT_TEST_FLOW_MSG_RECIEVE_20.bpmn"})
  public void sequence20MessageRecieve() {
    // Performance test external service task EXTERNAL_STEP_1
    performanceTest()
    .step(new StartProcessInstanceStep(engine, "NFT_TEST_FLOW_MSG_RECIEVE_20"))
	  .step(new CorrelateMessageStep(engine, "STEP_1", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_2", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_3", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_4", PerfTestConstants.PROCESS_INSTANCE_ID))    
	  .step(new CorrelateMessageStep(engine, "STEP_5", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_6", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_7", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_8", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_9", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_10", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_11", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_12", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_13", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_14", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_15", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_16", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_17", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_18", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_19", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .step(new CorrelateMessageStep(engine, "STEP_20", PerfTestConstants.PROCESS_INSTANCE_ID))
	  .run();
  }
    
}
