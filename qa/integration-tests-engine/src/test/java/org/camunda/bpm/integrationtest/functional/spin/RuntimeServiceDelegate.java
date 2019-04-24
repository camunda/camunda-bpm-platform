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
package org.camunda.bpm.integrationtest.functional.spin;

import java.util.Date;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.integrationtest.functional.spin.dataformat.JsonSerializable;

/**
 * @author Thorben Lindhauer
 *
 */
public class RuntimeServiceDelegate implements JavaDelegate {

  public static final String VARIABLE_NAME = "var";

  public void execute(DelegateExecution execution) throws Exception {
    RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

    ObjectValue jsonSerializeable = Variables
        .objectValue(createJsonSerializable())
        .serializationDataFormat(SerializationDataFormats.JSON)
        .create();

    // this should be executed in the context of the current process application
    runtimeService.setVariable(execution.getProcessInstanceId(), VARIABLE_NAME, jsonSerializeable);

  }

  public static JsonSerializable createJsonSerializable() {
    return new JsonSerializable(new Date(JsonSerializable.ONE_DAY_IN_MILLIS * 10));
  }
}
