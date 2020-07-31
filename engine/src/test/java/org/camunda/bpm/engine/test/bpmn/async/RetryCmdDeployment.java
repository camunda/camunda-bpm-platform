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
package org.camunda.bpm.engine.test.bpmn.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Askar Akhmerov
 */
public class RetryCmdDeployment {

  public static final String FAILING_EVENT = "failingEvent";
  public static final String PROCESS_ID = "failedIntermediateThrowingEventAsync";
  private static final String SCHEDULE = "R5/PT5M";
  private static final String PROCESS_ID_2 = "failingSignalProcess";
  public static final String MESSAGE = "start";
  private BpmnModelInstance[] bpmnModelInstances;

  public static RetryCmdDeployment deployment() {
    return new RetryCmdDeployment();
  }

  public static BpmnModelInstance prepareSignalEventProcess() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .camundaFailedJobRetryTimeCycle(SCHEDULE)
            .signal(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
    return modelInstance;
  }

  public static BpmnModelInstance prepareMessageEventProcess() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
              .camundaFailedJobRetryTimeCycle(SCHEDULE)
              .message(MESSAGE)
            .serviceTask()
              .camundaClass(FailingDelegate.class.getName())
        .done();
  }

  public static BpmnModelInstance prepareEscalationEventProcess() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .camundaFailedJobRetryTimeCycle(SCHEDULE)
            .escalation(MESSAGE)
          .serviceTask()
            .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }


  public static BpmnModelInstance prepareCompensationEventProcess() {
    return Bpmn.createExecutableProcess(PROCESS_ID)
        .startEvent()
          .subProcess("subProcess")
            .embeddedSubProcess()
              .startEvent()
              .endEvent()
          .subProcessDone()
          .intermediateThrowEvent(FAILING_EVENT)
            .camundaAsyncBefore(true)
            .camundaFailedJobRetryTimeCycle(SCHEDULE)
            .compensateEventDefinition()
            .compensateEventDefinitionDone()
          .serviceTask()
          .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }


  public RetryCmdDeployment withEventProcess(BpmnModelInstance... bpmnModelInstances) {
    this.bpmnModelInstances = bpmnModelInstances;
    return this;
  }

  public static Collection<RetryCmdDeployment[]> asParameters(RetryCmdDeployment... deployments) {
    List<RetryCmdDeployment[]> deploymentList = new ArrayList<RetryCmdDeployment[]>();
    for (RetryCmdDeployment deployment : deployments) {
      deploymentList.add(new RetryCmdDeployment[]{ deployment });
    }

    return deploymentList;
  }

  public BpmnModelInstance[] getBpmnModelInstances() {
    return bpmnModelInstances;
  }

  public void setBpmnModelInstances(BpmnModelInstance[] bpmnModelInstances) {
    this.bpmnModelInstances = bpmnModelInstances;
  }
}
