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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class SetAnnotationForOperationLog implements Command<Void> {

  protected String operationId;
  protected String annotation;

  public SetAnnotationForOperationLog(String operationId, String annotation) {
    this.operationId = operationId;
    this.annotation = annotation;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull(NotValidException.class, "operation id", operationId);

    UserOperationLogEntry operationLogEntry = commandContext.getOperationLogManager().findOperationLogByOperationId(operationId);
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "operation", operationLogEntry);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateUserOperationLog(operationLogEntry);
    }

    commandContext.getOperationLogManager()
        .updateOperationLogAnnotationByOperationId(operationId, annotation);

    if (annotation == null) {
      commandContext.getOperationLogManager()
          .logClearAnnotationOperation(operationId, operationLogEntry.getTenantId());

    } else {
      commandContext.getOperationLogManager()
          .logSetAnnotationOperation(operationId, operationLogEntry.getTenantId());
    }

    return null;
  }

}
