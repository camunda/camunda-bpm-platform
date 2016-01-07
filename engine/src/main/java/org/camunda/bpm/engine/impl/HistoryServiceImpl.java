/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricCaseInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.cmd.DeleteHistoricCaseInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteHistoricProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteHistoricTaskInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteUserOperationLogEntryCmd;
import org.camunda.bpm.engine.impl.cmd.GetHistoricJobLogExceptionStacktraceCmd;
import org.camunda.bpm.engine.impl.dmn.cmd.DeleteHistoricDecisionInstanceCmd;

/**
 * @author Tom Baeyens
 * @author Bernd Ruecker (camunda)
 * @author Christian Stettler
 */
public class HistoryServiceImpl extends ServiceImpl implements HistoryService {

  public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl(commandExecutor);
  }

  public HistoricActivityStatisticsQuery createHistoricActivityStatisticsQuery(String processDefinitionId) {
    return new HistoricActivityStatisticsQueryImpl(processDefinitionId, commandExecutor);
  }

  public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl(commandExecutor);
  }

  public HistoricDetailQuery createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl(commandExecutor);
  }

  public UserOperationLogQuery createUserOperationLogQuery() {
    return new UserOperationLogQueryImpl(commandExecutor);
  }

  public HistoricVariableInstanceQuery createHistoricVariableInstanceQuery() {
    return new HistoricVariableInstanceQueryImpl(commandExecutor);
  }

  public HistoricIncidentQuery createHistoricIncidentQuery() {
    return new HistoricIncidentQueryImpl(commandExecutor);
  }

  public HistoricCaseInstanceQuery createHistoricCaseInstanceQuery() {
    return new HistoricCaseInstanceQueryImpl(commandExecutor);
  }

  public HistoricCaseActivityInstanceQuery createHistoricCaseActivityInstanceQuery() {
    return new HistoricCaseActivityInstanceQueryImpl(commandExecutor);
  }

  public HistoricDecisionInstanceQuery createHistoricDecisionInstanceQuery() {
    return new HistoricDecisionInstanceQueryImpl(commandExecutor);
  }

  public void deleteHistoricTaskInstance(String taskId) {
    commandExecutor.execute(new DeleteHistoricTaskInstanceCmd(taskId));
  }

  public void deleteHistoricProcessInstance(String processInstanceId) {
    commandExecutor.execute(new DeleteHistoricProcessInstanceCmd(processInstanceId));
  }

  public void deleteUserOperationLogEntry(String entryId) {
    commandExecutor.execute(new DeleteUserOperationLogEntryCmd(entryId));
  }

  public void deleteHistoricCaseInstance(String caseInstanceId) {
    commandExecutor.execute(new DeleteHistoricCaseInstanceCmd(caseInstanceId));
  }

  public void deleteHistoricDecisionInstance(String decisionDefinitionId) {
    commandExecutor.execute(new DeleteHistoricDecisionInstanceCmd(decisionDefinitionId));
  }

  public NativeHistoricProcessInstanceQuery createNativeHistoricProcessInstanceQuery() {
    return new NativeHistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricTaskInstanceQuery createNativeHistoricTaskInstanceQuery() {
    return new NativeHistoricTaskInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricActivityInstanceQuery createNativeHistoricActivityInstanceQuery() {
    return new NativeHistoricActivityInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricCaseInstanceQuery createNativeHistoricCaseInstanceQuery() {
    return new NativeHistoricCaseInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricCaseActivityInstanceQuery createNativeHistoricCaseActivityInstanceQuery() {
    return new NativeHistoricCaseActivityInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricDecisionInstanceQuery createNativeHistoricDecisionInstanceQuery() {
    return new NativeHistoryDecisionInstanceQueryImpl(commandExecutor);
  }

  public HistoricJobLogQuery createHistoricJobLogQuery() {
    return new HistoricJobLogQueryImpl(commandExecutor);
  }

  public String getHistoricJobLogExceptionStacktrace(String historicJobLogId) {
    return commandExecutor.execute(new GetHistoricJobLogExceptionStacktraceCmd(historicJobLogId));
  }

  public HistoricProcessInstanceReport createHistoricProcessInstanceReport() {
    return new HistoricProcessInstanceReportImpl(commandExecutor);
  }

}
