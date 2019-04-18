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
package org.camunda.bpm.engine.test.history;

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class UpdateHistoricValueDelegate implements JavaDelegate, Serializable {

  private static final long serialVersionUID = 1L;

  public static final String NEW_ELEMENT = "new element";

  public void execute(DelegateExecution execution) throws Exception {
    HistoryService historyService = execution.getProcessEngineServices().getHistoryService();

    HistoricVariableInstance variableInstance = historyService
        .createHistoricVariableInstanceQuery()
        .variableName("listVar")
        .singleResult();

    List<String> list = (List<String>) variableInstance.getValue();

    // implicit update of the list, should not trigger an update
    // of the value since we deal with historic variables
    list.add(NEW_ELEMENT);
  }

}
