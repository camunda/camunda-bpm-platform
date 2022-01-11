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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

public class DeleteTaskMetricsCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected Date timestamp;

  public DeleteTaskMetricsCmd(Date timestamp) {
    this.timestamp = timestamp;
  }

  public Void execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkCamundaAdminOrPermission(CommandChecker::checkDeleteTaskMetrics);

    writeUserOperationLog(commandContext);
    commandContext.getMeterLogManager().deleteTaskMetricsByTimestamp(timestamp);
    return null;
  }

  protected void writeUserOperationLog(CommandContext commandContext) {
    List<PropertyChange> propertyChanges = new ArrayList<>();
    if (timestamp != null) {
      propertyChanges.add(new PropertyChange("timestamp", null, timestamp));
    }
    if (propertyChanges.isEmpty()) {
      propertyChanges.add(PropertyChange.EMPTY_CHANGE);
    }
    commandContext.getOperationLogManager().logTaskMetricsOperation(
        UserOperationLogEntry.OPERATION_TYPE_DELETE,
        propertyChanges);
  }

}
