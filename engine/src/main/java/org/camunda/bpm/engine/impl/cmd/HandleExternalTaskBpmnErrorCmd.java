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
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * Command to handle an external task BPMN error.
 *
 * @author Christopher Zell
 */
public class HandleExternalTaskBpmnErrorCmd extends HandleExternalTaskCmd {
  /**
   * The error code of the corresponding bpmn error.
   */
  protected String errorCode;
  protected String errorMessage;
  protected Map<String, Object> variables;

  public HandleExternalTaskBpmnErrorCmd(String externalTaskId, String workerId, String errorCode) {
    super(externalTaskId, workerId);
    this.errorCode = errorCode;
  }

  public HandleExternalTaskBpmnErrorCmd(String externalTaskId, String workerId, String errorCode, String errorMessage, Map<String, Object> variables) {
    super(externalTaskId, workerId);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.variables = variables;
  }

  @Override
  protected void validateInput() {
    super.validateInput();
    EnsureUtil.ensureNotNull("errorCode", errorCode);
  }

  @Override
  public String getErrorMessageOnWrongWorkerAccess() {
    return "Bpmn error of External Task " + externalTaskId + " cannot be reported by worker '" + workerId;
  }

  @Override
  public void execute(ExternalTaskEntity externalTask) {
    externalTask.bpmnError(errorCode, errorMessage, variables);
  }
}
