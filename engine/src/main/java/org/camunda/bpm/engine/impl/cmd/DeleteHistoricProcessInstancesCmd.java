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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Askar Akhmerov
 */
public class DeleteHistoricProcessInstancesCmd implements Command<Void>, Serializable {

  protected final List<String> processInstanceIds;
  protected final boolean failIfNotExists;

  public DeleteHistoricProcessInstancesCmd(List<String> processInstanceIds, boolean failIfNotExists) {
    this.processInstanceIds = processInstanceIds;
    this.failIfNotExists = failIfNotExists;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotEmpty(BadUserRequestException.class,"processInstanceIds", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class, "processInstanceId is null", "processInstanceIds", processInstanceIds);

    // Check if process instance is still running
    List<HistoricProcessInstance> instances = commandContext.runWithoutAuthorization(new Callable<List<HistoricProcessInstance>>() {
      @Override
      public List<HistoricProcessInstance> call() throws Exception {
        return new HistoricProcessInstanceQueryImpl().processInstanceIds(new HashSet<String>(processInstanceIds)).list();
      }
    });

    if (failIfNotExists) {
      if (processInstanceIds.size() == 1) {
        ensureNotEmpty(BadUserRequestException.class, "No historic process instance found with id: " + processInstanceIds.get(0), "historicProcessInstanceIds",
            instances);
      } else {
        ensureNotEmpty(BadUserRequestException.class, "No historic process instances found", "historicProcessInstanceIds", instances);
      }
    }

    List<String> existingIds = new ArrayList<String>();

    for (HistoricProcessInstance historicProcessInstance : instances) {
      existingIds.add(historicProcessInstance.getId());

      for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkDeleteHistoricProcessInstance(historicProcessInstance);
      }

      ensureNotNull(BadUserRequestException.class, "Process instance is still running, cannot delete historic process instance: " + historicProcessInstance, "instance.getEndTime()", historicProcessInstance.getEndTime());
    }

    if(failIfNotExists) {
      ArrayList<String> nonExistingIds = new ArrayList<String>(processInstanceIds);
      nonExistingIds.removeAll(existingIds);
      if(nonExistingIds.size() != 0) {
        throw new BadUserRequestException("No historic process instance found with id: " + nonExistingIds);
      }
    }

    if(existingIds.size() > 0) {
      commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(existingIds);
    }
    writeUserOperationLog(commandContext, existingIds.size());

    return null;
  }

  protected void writeUserOperationLog(CommandContext commandContext, int numInstances) {

    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, false));

    commandContext.getOperationLogManager()
      .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY,
        null,
        null,
        null,
        propertyChanges);
  }
}
