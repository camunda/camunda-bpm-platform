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
package org.camunda.bpm.engine.impl.cmd;

import java.util.Map;

import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public class CompleteExternalTaskCmd extends HandleExternalTaskCmd {

  protected Map<String, Object> variables;
  protected Map<String, Object> localVariables;

  public CompleteExternalTaskCmd(String externalTaskId, String workerId, Map<String, Object> variables, Map<String, Object> localVariables) {
    super(externalTaskId, workerId);
    this.localVariables = localVariables;
    this.variables = variables;
  }

  @Override
  public String getErrorMessageOnWrongWorkerAccess() {
    return "External Task " + externalTaskId + " cannot be completed by worker '" + workerId;
  }

  @Override
  public void execute(ExternalTaskEntity externalTask) {
    externalTask.complete(variables, localVariables);
  }
}
