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
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessInstancesCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.SuspensionBuilder;

public class SuspensionBuilderImpl implements SuspensionBuilder {

  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;
  protected CommandExecutor commandExecutor;
  protected String processDefinitionId;
  protected boolean suspend;

  public SuspensionBuilderImpl(CommandExecutor commandExecutor, boolean suspend) {
    this.processInstanceIds = new ArrayList<String>();
    this.commandExecutor = commandExecutor;
    this.suspend = suspend;
  }

  @Override
  public SuspensionBuilder processInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds.addAll(processInstanceIds);
    return this;
  }

  @Override
  public SuspensionBuilder processInstanceIds(String... processInstanceIds) {
    this.processInstanceIds.addAll(Arrays.asList(processInstanceIds));
    return this;
  }



  @Override
  public SuspensionBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
    return this;

  }

  @Override
  public SuspensionBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
    return this;

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

  public boolean getSuspendState() {
    return suspend;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @Override
  public void setSuspendState(boolean suspend) {
    this.suspend = suspend;
  }

  @Override
  public void execute() {
    commandExecutor.execute(new SuspendProcessInstancesCmd(commandExecutor, this));

  }
}
