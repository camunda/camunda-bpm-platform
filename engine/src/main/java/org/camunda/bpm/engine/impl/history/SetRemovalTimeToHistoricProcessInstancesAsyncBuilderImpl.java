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
package org.camunda.bpm.engine.impl.history;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricProcessInstancesAsyncBuilder;
import org.camunda.bpm.engine.impl.cmd.batch.SetRemovalTimeToHistoricProcessInstancesCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.Date;

/**
 * @author Tassilo Weidner
 */
public class SetRemovalTimeToHistoricProcessInstancesAsyncBuilderImpl implements SetRemovalTimeToHistoricProcessInstancesAsyncBuilder {

  protected HistoricProcessInstanceQuery query;
  protected Date removalTime;
  protected boolean hasRemovalTime = false;

  protected CommandExecutor commandExecutor;

  public SetRemovalTimeToHistoricProcessInstancesAsyncBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public SetRemovalTimeToHistoricProcessInstancesAsyncBuilder byQuery(HistoricProcessInstanceQuery query) {
    this.query = query;
    return this;
  }

  public SetRemovalTimeToHistoricProcessInstancesAsyncBuilder removalTime(Date removalTime) {
    hasRemovalTime = true;
    this.removalTime = removalTime;
    return this;
  }

  public Batch executeAsync() {
    return commandExecutor.execute(new SetRemovalTimeToHistoricProcessInstancesCmd(this));
  }

  public HistoricProcessInstanceQuery getQuery() {
    return query;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public boolean hasRemovalTime() {
    return hasRemovalTime;
  }

}
