/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.UpdateProcessInstancesSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

public abstract class AbstractUpdateProcessInstancesSuspendStateCmd<T> implements Command<T> {

  protected UpdateProcessInstancesSuspensionStateBuilderImpl builder;
  protected CommandExecutor commandExecutor;
  protected boolean suspending;

  public AbstractUpdateProcessInstancesSuspendStateCmd(CommandExecutor commandExecutor, UpdateProcessInstancesSuspensionStateBuilderImpl builder, boolean suspending) {
    this.commandExecutor = commandExecutor;
    this.builder = builder;
    this.suspending = suspending;
  }

  protected Collection<String> collectProcessInstanceIds() {
    HashSet<String> allProcessInstanceIds = new HashSet<String>();

    List<String> processInstanceIds = builder.getProcessInstanceIds();
    if (processInstanceIds != null) {
      allProcessInstanceIds.addAll(processInstanceIds);
    }

    ProcessInstanceQueryImpl processInstanceQuery = (ProcessInstanceQueryImpl) builder.getProcessInstanceQuery();
    if( processInstanceQuery != null) {
      allProcessInstanceIds.addAll(processInstanceQuery.listIds());
    }

    HistoricProcessInstanceQueryImpl historicProcessInstanceQuery = (HistoricProcessInstanceQueryImpl ) builder.getHistoricProcessInstanceQuery();
    if( historicProcessInstanceQuery != null) {
      allProcessInstanceIds.addAll(historicProcessInstanceQuery.listIds());
    }

    return allProcessInstanceIds;
  }

  protected void writeUserOperationLog(CommandContext commandContext,
                                       int numInstances,
                                       boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances",
      null,
      numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));

    String operationType;
    if(suspending) {
      operationType = UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB;

    } else {
      operationType = UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB;
    }
    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(operationType,
          null,
          null,
          null,
          propertyChanges);
  }
}
