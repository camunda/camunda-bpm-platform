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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Arrays;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Sebastian Menski
 */
public class DeleteHistoricCaseInstanceCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseInstanceId;

  public DeleteHistoricCaseInstanceCmd(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public Object execute(CommandContext commandContext) {
    ensureNotNull("caseInstanceId", caseInstanceId);
    // Check if case instance is still running
    HistoricCaseInstance instance = commandContext
      .getHistoricCaseInstanceManager()
      .findHistoricCaseInstance(caseInstanceId);

    ensureNotNull("No historic case instance found with id: " + caseInstanceId, "instance", instance);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkDeleteHistoricCaseInstance(instance);
    }

    ensureNotNull("Case instance is still running, cannot delete historic case instance: " + caseInstanceId, "instance.getCloseTime()", instance.getCloseTime());

    commandContext
      .getHistoricCaseInstanceManager()
      .deleteHistoricCaseInstancesByIds(Arrays.asList(caseInstanceId));

    return null;
  }

}
