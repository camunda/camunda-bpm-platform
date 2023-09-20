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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.qa.performance.engine.bpmn.delegate.NoopDelegate;
import org.camunda.bpm.qa.performance.engine.junit.ProcessEnginePerformanceTestCase;
import org.camunda.bpm.qa.performance.engine.steps.StartProcessInstanceStep;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class ServiceTaskPerformanceTest extends ProcessEnginePerformanceTestCase {

  @Test
  public void threeServiceTasksAndAGateway() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("approved", true);

    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .camundaHistoryTimeToLive(180)
        .startEvent()
      .serviceTask()
        .camundaClass(NoopDelegate.class.getName())
      .exclusiveGateway("decision").condition("approved", "${approved}")
      .serviceTask()
        .camundaClass(NoopDelegate.class.getName())
      .moveToLastGateway().condition("not approved", "${not approved}")
      .serviceTask()
        .camundaClass(NoopDelegate.class.getName())
      .endEvent()
      .done();

    Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("process.bpmn", process)
      .deploy();

    performanceTest()
      .step(new StartProcessInstanceStep(engine, "process", variables))
    .run();

  }

}
