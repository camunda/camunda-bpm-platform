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
package org.camunda.bpm.engine.test.api.runtime;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SendMessageDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
    Boolean allFlag = (Boolean) execution.getVariable("allFlag");

    if (allFlag) {
      // 1. message
      Map<String, Object> variablesFirstCall = new HashMap<String, Object>();
      variablesFirstCall.put("someVariable", "someValue1");
      runtimeService.createMessageCorrelation("waitForCorrelationKeyMessage")
              .setVariables(variablesFirstCall)
              .processInstanceVariableEquals("correlationKey", "someCorrelationKey")
              .correlateAllWithResult();

      // 2. message
      Map<String, Object> variablesSecondCall = new HashMap<String, Object>();
      variablesSecondCall.put("someVariable", "someValue2");
      runtimeService.createMessageCorrelation("waitForCorrelationKeyMessage")
              .setVariables(variablesSecondCall)
              .processInstanceVariableEquals("correlationKey", "someCorrelationKey")
              .correlateAllWithResult();
    } else {
      // 1. message
      Map<String, Object> variablesFirstCall = new HashMap<String, Object>();
      variablesFirstCall.put("someVariable", "someValue1");
      runtimeService.createMessageCorrelation("waitForCorrelationKeyMessage")
              .setVariables(variablesFirstCall)
              .processInstanceVariableEquals("correlationKey", "someCorrelationKey")
              .correlateWithResult();

      // 2. message
      Map<String, Object> variablesSecondCall = new HashMap<String, Object>();
      variablesSecondCall.put("someVariable", "someValue2");
      runtimeService.createMessageCorrelation("waitForCorrelationKeyMessage")
              .setVariables(variablesSecondCall)
              .processInstanceVariableEquals("correlationKey", "someCorrelationKey")
              .correlateWithResult();
    }

  }
}
