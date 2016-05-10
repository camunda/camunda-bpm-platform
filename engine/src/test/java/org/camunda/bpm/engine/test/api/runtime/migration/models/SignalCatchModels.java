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
public class SignalCatchModels {

  public static final String SIGNAL_NAME = "Signal";

  public static final BpmnModelInstance ONE_SIGNAL_CATCH_PROCESS = ProcessModels.newModel()
    .startEvent()
    .intermediateCatchEvent("signalCatch")
      .signal(SIGNAL_NAME)
    .userTask("userTask")
    .endEvent()
    .done();

  public static final BpmnModelInstance SUBPROCESS_SIGNAL_CATCH_PROCESS = ProcessModels.newModel()
      .startEvent()
      .subProcess("subProcess")
      .embeddedSubProcess()
        .startEvent()
        .intermediateCatchEvent("signalCatch")
          .signal(SIGNAL_NAME)
        .userTask("userTask")
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
}
