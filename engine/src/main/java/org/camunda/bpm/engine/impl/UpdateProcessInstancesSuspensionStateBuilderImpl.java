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
  protected boolean suspend;

  public UpdateProcessInstancesSuspensionStateBuilderImpl(CommandExecutor commandExecutor, boolean suspend) {
    this.processInstanceIds = new ArrayList<String>();
    this.commandExecutor = commandExecutor;
    this.suspend = suspend;
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
    this.suspend = true;
    commandExecutor.execute(new UpdateProcessInstancesSuspendStateCmd(commandExecutor, this, suspend));
  }

  @Override
  public void activate() {
    this.suspend = false;
    commandExecutor.execute(new UpdateProcessInstancesSuspendStateCmd(commandExecutor, this, suspend));
  }

  @Override
  public Batch suspendAsync() {
    this.suspend = true;
    return commandExecutor.execute(new UpdateProcessInstancesSuspendStateBatchCmd(commandExecutor, this, suspend));
  }

  @Override
  public Batch activateAsync() {
    this.suspend = false;
    return commandExecutor.execute(new UpdateProcessInstancesSuspendStateBatchCmd(commandExecutor, this, suspend));

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
