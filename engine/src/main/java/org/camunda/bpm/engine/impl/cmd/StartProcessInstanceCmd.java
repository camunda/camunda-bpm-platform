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
import java.util.Collections;
import org.camunda.bpm.engine.history.UserOperationLogEntry;

import org.camunda.bpm.engine.impl.ProcessInstantiationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionVariableSnapshotObserver;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class StartProcessInstanceCmd implements Command<ProcessInstanceWithVariables>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final ProcessInstantiationBuilderImpl instantiationBuilder;

  public StartProcessInstanceCmd(ProcessInstantiationBuilderImpl instantiationBuilder) {
    this.instantiationBuilder = instantiationBuilder;
  }

  public ProcessInstanceWithVariables execute(CommandContext commandContext) {

    ProcessDefinitionEntity processDefinition = new GetDeployedProcessDefinitionCmd(instantiationBuilder, false).execute(commandContext);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateProcessInstance(processDefinition);
    }

    // Start the process instance
    ExecutionEntity processInstance = processDefinition.createProcessInstance(instantiationBuilder.getBusinessKey(),
        instantiationBuilder.getCaseInstanceId());

    if (instantiationBuilder.getTenantId() != null) {
      processInstance.setTenantId(instantiationBuilder.getTenantId());
    }

    final ExecutionVariableSnapshotObserver variablesListener = new ExecutionVariableSnapshotObserver(processInstance);

    processInstance.start(instantiationBuilder.getVariables());

    commandContext.getOperationLogManager().logProcessInstanceOperation(
        UserOperationLogEntry.OPERATION_TYPE_CREATE,
        processInstance.getId(),
        processInstance.getProcessDefinitionId(),
        processInstance.getProcessDefinition().getKey(),
        Collections.singletonList(PropertyChange.EMPTY_CHANGE));

    return new ProcessInstanceWithVariablesImpl(processInstance, variablesListener.getVariables());
  }

}
