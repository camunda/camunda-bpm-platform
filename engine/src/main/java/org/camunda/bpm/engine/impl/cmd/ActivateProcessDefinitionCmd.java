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

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.management.UpdateJobDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.repository.UpdateProcessDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.runtime.UpdateProcessInstanceSuspensionStateBuilderImpl;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public class ActivateProcessDefinitionCmd extends AbstractSetProcessDefinitionStateCmd {

  public ActivateProcessDefinitionCmd(UpdateProcessDefinitionSuspensionStateBuilderImpl builder) {
    super(builder);
  }

  @Override
  protected SuspensionState getNewSuspensionState() {
    return SuspensionState.ACTIVE;
  }

  @Override
  protected String getDelayedExecutionJobHandlerType() {
    return TimerActivateProcessDefinitionHandler.TYPE;
  }

  @Override
  protected AbstractSetJobDefinitionStateCmd getSetJobDefinitionStateCmd(UpdateJobDefinitionSuspensionStateBuilderImpl jobDefinitionSuspensionStateBuilder) {
    return new ActivateJobDefinitionCmd(jobDefinitionSuspensionStateBuilder);
  }

  @Override
  protected ActivateProcessInstanceCmd getNextCommand(UpdateProcessInstanceSuspensionStateBuilderImpl processInstanceCommandBuilder) {
    return new ActivateProcessInstanceCmd(processInstanceCommandBuilder);
  }

  @Override
  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION;
  }

}
