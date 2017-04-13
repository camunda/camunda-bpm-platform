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

import java.io.Serializable;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureGreaterThanOrEqual;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Svetlana Dorokhova
 */
public class UpdateProcessDefinitionTimeToLiveCmd implements Command<Void>, Serializable {

	private static final long serialVersionUID = 1L;

	protected String processDefinitionId;
  protected Integer timeToLive;

	public UpdateProcessDefinitionTimeToLiveCmd(String processDefinitionId, Integer timeToLive) {
		this.processDefinitionId = processDefinitionId;
		this.timeToLive = timeToLive;
	}

	public Void execute(CommandContext commandContext) {
    checkAuthorization(commandContext);

    ensureNotNull(BadUserRequestException.class, "processDefinitionId", processDefinitionId);
    if (timeToLive != null) {
      ensureGreaterThanOrEqual(BadUserRequestException.class, "", "timeToLive", timeToLive, 0);
    }

    ProcessDefinitionEntity processDefinitionEntity = commandContext.getDbEntityManager().selectById(ProcessDefinitionEntity.class, processDefinitionId);
    processDefinitionEntity.setTimeToLive(timeToLive);
    commandContext.getProcessDefinitionManager().updateProcessDefinition(processDefinitionEntity);

    return null;
  }

  protected void checkAuthorization(CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateProcessDefinitionById(processDefinitionId);
    }
  }

}
