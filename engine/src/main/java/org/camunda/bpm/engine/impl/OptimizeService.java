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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeCompletedHistoricActivityInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeCompletedHistoricProcessInstanceQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeHistoricVariableUpdateQueryCmd;
import org.camunda.bpm.engine.impl.cmd.optimize.OptimizeRunningHistoricProcessInstanceQueryCmd;

import java.util.Date;
import java.util.List;

public class OptimizeService extends ServiceImpl {

  public List<HistoricActivityInstance> getCompletedHistoricActivityInstances(Date finishedAfter,
                                                                              Date finishedAt,
                                                                              int maxResults) {
    return commandExecutor.execute(
      new OptimizeCompletedHistoricActivityInstanceQueryCmd(finishedAfter, finishedAt,maxResults)
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
                                                                 int maxResults) {
    return commandExecutor.execute(
      new OptimizeHistoricVariableUpdateQueryCmd(occurredAfter, occurredAt,maxResults)
    );
  }



}
