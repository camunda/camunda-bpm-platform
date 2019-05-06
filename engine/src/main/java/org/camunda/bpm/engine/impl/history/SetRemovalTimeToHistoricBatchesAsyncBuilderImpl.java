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
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricBatchesAsyncBuilder;
import org.camunda.bpm.engine.impl.cmd.batch.removaltime.SetRemovalTimeToHistoricBatchesCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.Date;

/**
 * @author Tassilo Weidner
 */
public class SetRemovalTimeToHistoricBatchesAsyncBuilderImpl implements SetRemovalTimeToHistoricBatchesAsyncBuilder {

  protected HistoricBatchQuery query;
  protected Mode mode = null;
  protected Date removalTime;

  protected CommandExecutor commandExecutor;

  public SetRemovalTimeToHistoricBatchesAsyncBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public SetRemovalTimeToHistoricBatchesAsyncBuilder byQuery(HistoricBatchQuery query) {
    this.query = query;
    return this;
  }

  public SetRemovalTimeToHistoricBatchesAsyncBuilder absoluteRemovalTime(Date removalTime) {
    this.mode = Mode.ABSOLUTE_REMOVAL_TIME;
    this.removalTime = removalTime;
    return this;
  }

  public SetRemovalTimeToHistoricBatchesAsyncBuilder calculatedRemovalTime() {
    this.mode = Mode.CALCULATED_REMOVAL_TIME;
    return this;
  }

  public Batch executeAsync() {
    return commandExecutor.execute(new SetRemovalTimeToHistoricBatchesCmd(this));
  }

  public HistoricBatchQuery getQuery() {
    return query;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public Mode getMode() {
    return mode;
  }

  public enum Mode
  {
    CALCULATED_REMOVAL_TIME,
    ABSOLUTE_REMOVAL_TIME;
  }

}
