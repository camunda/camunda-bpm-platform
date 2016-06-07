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

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.impl.db.TenantCheck;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;
import org.camunda.bpm.engine.task.TaskReport;

/**
 * @author Stefan Hentschel
 *
 */
public class TaskReportImpl implements Serializable, TaskReport {

  private static final long serialVersionUID = 1L;

  protected transient CommandExecutor commandExecutor;

  protected TenantCheck tenantCheck = new TenantCheck();

  public TaskReportImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  protected List<TaskCountByCandidateGroupResult> createTaskCountByCandidateGroupReport(CommandContext commandContext) {
    return commandContext
        .getTaskReportManager()
        .createTaskCountByCandidateGroupReport(this);
  }

  public TenantCheck getTenantCheck() {
    return tenantCheck;
  }

  public List<TaskCountByCandidateGroupResult> taskCountByCandidateGroup() {
    return commandExecutor.execute(new Command<List<TaskCountByCandidateGroupResult>>() {
      @Override
      public List<TaskCountByCandidateGroupResult> execute(CommandContext commandContext) {
        return createTaskCountByCandidateGroupReport(commandContext);
      }
    });
  }

}
