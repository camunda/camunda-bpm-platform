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
package org.camunda.bpm.engine.test.api.runtime.migration.models;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class CallActivityModels {

  public static BpmnModelInstance oneBpmnCallActivityProcess(String calledProcessKey) {
    return ProcessModels.newModel()
        .startEvent()
        .callActivity("callActivity")
          .calledElement(calledProcessKey)
        .userTask("userTask")
        .endEvent()
        .done();
  }

  public static BpmnModelInstance subProcessBpmnCallActivityProcess(String calledProcessKey) {
    return ProcessModels.newModel()
        .startEvent()
        .subProcess("subProcess")
        .embeddedSubProcess()
          .startEvent()
          .callActivity("callActivity")
            .calledElement(calledProcessKey)
          .userTask("userTask")
          .endEvent()
        .subProcessDone()
        .endEvent()
        .done();
  }

  public static BpmnModelInstance oneCmmnCallActivityProcess(String caseCaseKey) {
    return ProcessModels.newModel()
        .startEvent()
        .callActivity("callActivity")
          .camundaCaseRef(caseCaseKey)
        .userTask("userTask")
        .endEvent()
        .done();
  }
}
