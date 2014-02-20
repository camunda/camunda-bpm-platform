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

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * @author Daniel Meyer
 *
 */
public class AddGroupIdentityLinkCmd extends AddIdentityLinkCmd {

  private static final long serialVersionUID = 1L;

  public AddGroupIdentityLinkCmd(String taskId, String groupId, String type) {
    super(taskId, null, groupId, type);
  }

  @Override
  public Void execute(CommandContext commandContext) {
    super.execute(commandContext);

    PropertyChange propertyChange = new PropertyChange(type, null, groupId);

    commandContext.getOperationLogManager()
      .logLinkOperation(UserOperationLogEntry.OPERATION_TYPE_ADD_GROUP_LINK, task, propertyChange);

    return null;
  }

}
