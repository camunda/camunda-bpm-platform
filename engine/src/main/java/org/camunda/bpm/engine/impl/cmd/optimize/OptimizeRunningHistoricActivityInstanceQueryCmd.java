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
package org.camunda.bpm.engine.impl.cmd.optimize;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.util.Date;
import java.util.List;

public class OptimizeRunningHistoricActivityInstanceQueryCmd implements Command<List<HistoricActivityInstance>> {

  protected Date startedAfter;
  protected Date startedAt;
  protected int maxResults;

  public OptimizeRunningHistoricActivityInstanceQueryCmd(Date startedAfter, Date startedAt, int maxResults) {
    this.startedAfter = startedAfter;
    this.startedAt = startedAt;
    this.maxResults = maxResults;
  }

  public List<HistoricActivityInstance> execute(CommandContext commandContext) {
    return commandContext.getOptimizeManager().getRunningHistoricActivityInstances(startedAfter, startedAt, maxResults);
  }

}
