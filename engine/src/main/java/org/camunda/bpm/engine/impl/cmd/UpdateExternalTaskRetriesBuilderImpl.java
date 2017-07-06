/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesBuilder;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

/**
 * @author smirnov
 *
 */
public class UpdateExternalTaskRetriesBuilderImpl implements UpdateExternalTaskRetriesBuilder {

  protected CommandExecutor commandExecutor;

  protected List<String> externalTaskIds;
  protected List<String> processInstanceIds;

  protected ExternalTaskQuery externalTaskQuery;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;

  protected int retries;

  public UpdateExternalTaskRetriesBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public UpdateExternalTaskRetriesBuilder externalTaskIds(List<String> externalTaskIds) {
    this.externalTaskIds = externalTaskIds;
    return this;
  }

  public UpdateExternalTaskRetriesBuilder externalTaskIds(String... externalTaskIds) {
    if (externalTaskIds == null) {
      this.externalTaskIds = Collections.emptyList();
    }
    else {
      this.externalTaskIds = Arrays.asList(externalTaskIds);
    }
    return this;
  }

  public UpdateExternalTaskRetriesBuilder processInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public UpdateExternalTaskRetriesBuilder processInstanceIds(String... processInstanceIds) {
    if (processInstanceIds == null) {
      this.processInstanceIds = Collections.emptyList();
    }
    else {
      this.processInstanceIds = Arrays.asList(processInstanceIds);
    }
    return this;
  }

  public UpdateExternalTaskRetriesBuilder externalTaskQuery(ExternalTaskQuery externalTaskQuery) {
    this.externalTaskQuery = externalTaskQuery;
    return this;
  }

  public UpdateExternalTaskRetriesBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
    return this;
  }

  public UpdateExternalTaskRetriesBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
    return this;
  }

  public void set(int retries) {
    this.retries = retries;
    commandExecutor.execute(new SetExternalTasksRetriesCmd(this));
  }

  @Override
  public Batch setAsync(int retries) {
    this.retries = retries;
    return commandExecutor.execute(new SetExternalTasksRetriesBatchCmd(this));
  }

  public int getRetries() {
    return retries;
  }

  public List<String> getExternalTaskIds() {
    return externalTaskIds;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public ExternalTaskQuery getExternalTaskQuery() {
    return externalTaskQuery;
  }

  public ProcessInstanceQuery getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public HistoricProcessInstanceQuery getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

}
