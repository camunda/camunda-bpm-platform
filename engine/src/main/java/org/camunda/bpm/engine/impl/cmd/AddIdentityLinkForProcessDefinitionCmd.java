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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tijs Rademakers
 */
public class AddIdentityLinkForProcessDefinitionCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;
  
  protected String userId;
  
  protected String groupId;
  
  public AddIdentityLinkForProcessDefinitionCmd(String processDefinitionId, String userId, String groupId) {
    validateParams(userId, groupId, processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    this.userId = userId;
    this.groupId = groupId;
  }
  
  protected void validateParams(String userId, String groupId, String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);

    if (userId == null && groupId == null) {
      throw new ProcessEngineException("userId and groupId cannot both be null");
    }
  }
  
  public Void execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findLatestProcessDefinitionById(processDefinitionId);

    EnsureUtil.ensureNotNull("Cannot find process definition with id " + processDefinitionId, "processDefinition", processDefinition);

    processDefinition.addIdentityLink(userId, groupId);
    return null;
  }
  
}
