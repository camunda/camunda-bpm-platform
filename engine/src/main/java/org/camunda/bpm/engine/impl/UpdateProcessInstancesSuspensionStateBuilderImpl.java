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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.cmd.UpdateProcessInstancesSuspendStateBatchCmd;
import org.camunda.bpm.engine.impl.cmd.UpdateProcessInstancesSuspendStateCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.UpdateProcessInstancesSuspensionStateBuilder;

public class UpdateProcessInstancesSuspensionStateBuilderImpl implements UpdateProcessInstancesSuspensionStateBuilder {

  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;
  protected CommandExecutor commandExecutor;
  protected String processDefinitionId;

  public UpdateProcessInstancesSuspensionStateBuilderImpl(CommandExecutor commandExecutor) {
    this.processInstanceIds = new ArrayList<String>();
    this.commandExecutor = commandExecutor;
  }

  public UpdateProcessInstancesSuspensionStateBuilderImpl(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds.addAll(processInstanceIds);
    return this;
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byProcessInstanceIds(String... processInstanceIds) {
    this.processInstanceIds.addAll(Arrays.asList(processInstanceIds));
    return this;
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byProcessInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
    return this;
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byHistoricProcessInstanceQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
    return this;
  }

  @Override
  public void suspend() {
    commandExecutor.execute(new UpdateProcessInstancesSuspendStateCmd(commandExecutor, this, true));
  }

  @Override
  public void activate() {
    commandExecutor.execute(new UpdateProcessInstancesSuspendStateCmd(commandExecutor, this, false));
  }

  @Override
  public Batch suspendAsync() {
    return commandExecutor.execute(new UpdateProcessInstancesSuspendStateBatchCmd(commandExecutor, this, true));
  }

  @Override
  public Batch activateAsync() {
    return commandExecutor.execute(new UpdateProcessInstancesSuspendStateBatchCmd(commandExecutor, this, false));
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public ProcessInstanceQuery getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public HistoricProcessInstanceQuery getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

}
