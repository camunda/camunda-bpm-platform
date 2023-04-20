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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * @author Sebastian Menski
 */
public class DeleteHistoricCaseInstanceCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseInstanceId;

  public DeleteHistoricCaseInstanceCmd(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public Object execute(CommandContext commandContext) {
    ensureNotNull("caseInstanceId", caseInstanceId);
    // Check if case instance is still running
    HistoricCaseInstance instance = commandContext
      .getHistoricCaseInstanceManager()
      .findHistoricCaseInstance(caseInstanceId);

    ensureNotNull("No historic case instance found with id: " + caseInstanceId, "instance", instance);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteHistoricCaseInstance(instance);
    }

    ensureNotNull("Case instance is still running, cannot delete historic case instance: " + caseInstanceId, "instance.getCloseTime()", instance.getCloseTime());

    commandContext.getOperationLogManager().logCaseInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY,
        caseInstanceId,
        instance.getTenantId(),
        Collections.singletonList(PropertyChange.EMPTY_CHANGE));

    commandContext
      .getHistoricCaseInstanceManager()
      .deleteHistoricCaseInstancesByIds(Arrays.asList(caseInstanceId));

    return null;
  }

}
