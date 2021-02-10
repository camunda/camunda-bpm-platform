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

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeCompletedHistoricActivityInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeCompletedHistoricIncidentsQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeCompletedHistoricProcessInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeCompletedHistoricTaskInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeHistoricDecisionInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeHistoricIdentityLinkLogQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeHistoricUserOperationsLogQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeHistoricVariableUpdateQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeOpenHistoricIncidentsQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeRunningHistoricActivityInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeRunningHistoricProcessInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeRunningHistoricTaskInstanceQueryCmd;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.optimize.OptimizeHistoricIdentityLinkLogEntity;

import java.util.Date;
import java.util.List;

public class OptimizeService extends ServiceImpl {

  public List<HistoricActivityInstance> getCompletedHistoricActivityInstances(Date finishedAfter,
                                                                              Date finishedAt,
                                                                              int maxResults) {
    return commandExecutor.execute(
      new OptimizeCompletedHistoricActivityInstanceQueryCmd(finishedAfter, finishedAt, maxResults)
    );
  }

  public List<HistoricActivityInstance> getRunningHistoricActivityInstances(Date startedAfter,
                                                                            Date startedAt,
                                                                            int maxResults) {
    return commandExecutor.execute(
      new OptimizeRunningHistoricActivityInstanceQueryCmd(startedAfter, startedAt, maxResults)
    );
  }

  public List<HistoricTaskInstance> getCompletedHistoricTaskInstances(Date finishedAfter,
                                                                      Date finishedAt,
                                                                      int maxResults) {
    return commandExecutor.execute(
      new OptimizeCompletedHistoricTaskInstanceQueryCmd(finishedAfter, finishedAt, maxResults)
    );
  }

  public List<HistoricTaskInstance> getRunningHistoricTaskInstances(Date startedAfter,
                                                                    Date startedAt,
                                                                    int maxResults) {
    return commandExecutor.execute(
      new OptimizeRunningHistoricTaskInstanceQueryCmd(startedAfter, startedAt, maxResults)
    );
  }

  public List<UserOperationLogEntry> getHistoricUserOperationLogs(Date occurredAfter,
                                                                  Date occurredAt,
                                                                  int maxResults) {
    return commandExecutor.execute(
      new OptimizeHistoricUserOperationsLogQueryCmd(occurredAfter, occurredAt, maxResults)
    );
  }

  public List<OptimizeHistoricIdentityLinkLogEntity> getHistoricIdentityLinkLogs(Date occurredAfter,
                                                                                 Date occurredAt,
                                                                                 int maxResults) {
    return commandExecutor.execute(
      new OptimizeHistoricIdentityLinkLogQueryCmd(occurredAfter, occurredAt, maxResults)
    );
  }

  public List<HistoricProcessInstance> getCompletedHistoricProcessInstances(Date finishedAfter,
                                                                            Date finishedAt,
                                                                            int maxResults) {
    return commandExecutor.execute(
      new OptimizeCompletedHistoricProcessInstanceQueryCmd(finishedAfter, finishedAt, maxResults)
    );
  }

  public List<HistoricProcessInstance> getRunningHistoricProcessInstances(Date startedAfter,
                                                                          Date startedAt,
                                                                          int maxResults) {
    return commandExecutor.execute(
      new OptimizeRunningHistoricProcessInstanceQueryCmd(startedAfter, startedAt, maxResults)
    );
  }

  public List<HistoricVariableUpdate> getHistoricVariableUpdates(Date occurredAfter,
                                                                 Date occurredAt,
                                                                 boolean excludeObjectValues,
                                                                 int maxResults) {
    return commandExecutor.execute(
      new OptimizeHistoricVariableUpdateQueryCmd(occurredAfter, occurredAt, excludeObjectValues, maxResults)
    );
  }

  public List<HistoricIncidentEntity> getCompletedHistoricIncidents(Date finishedAfter,
                                                                    Date finishedAt,
                                                                    int maxResults) {
    return commandExecutor.execute(
      new OptimizeCompletedHistoricIncidentsQueryCmd(finishedAfter, finishedAt, maxResults)
    );
  }

  public List<HistoricIncidentEntity> getOpenHistoricIncidents(Date createdAfter,
                                                               Date createdAt,
                                                               int maxResults) {
    return commandExecutor.execute(
      new OptimizeOpenHistoricIncidentsQueryCmd(createdAfter, createdAt, maxResults)
    );
  }

  public List<HistoricDecisionInstance> getHistoricDecisionInstances(Date evaluatedAfter,
                                                                     Date evaluatedAt,
                                                                     int maxResults) {
    return commandExecutor.execute(
      new OptimizeHistoricDecisionInstanceQueryCmd(evaluatedAfter, evaluatedAt, maxResults)
    );
  }


}
