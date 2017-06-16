/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.cmd;

import java.util.Collection;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.UpdateProcessInstancesSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.runtime.UpdateProcessInstanceSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class UpdateProcessInstancesSuspendStateCmd extends AbstractUpdateProcessInstancesSuspendStateCmd<Void> {

  public UpdateProcessInstancesSuspendStateCmd(CommandExecutor commandExecutor, UpdateProcessInstancesSuspensionStateBuilderImpl builder, boolean suspendstate) {
    super(commandExecutor, builder, suspendstate);
  }

  public Void execute(CommandContext commandContext) {

    Collection<String> processInstanceIds = collectProcessInstanceIds();

    EnsureUtil.ensureNotEmpty(BadUserRequestException.class, "No process instance ids given", "Process Instance ids", processInstanceIds);
    EnsureUtil.ensureNotContainsNull(BadUserRequestException.class, "Cannot be null.", "Process Instance ids", processInstanceIds);

    writeUserOperationLog(commandContext, processInstanceIds.size(), false);

    UpdateProcessInstanceSuspensionStateBuilderImpl suspensionStateBuilder = new UpdateProcessInstanceSuspensionStateBuilderImpl(commandExecutor);
    if (suspending) {
      // suspending
      for (String processInstanceId : processInstanceIds) {
        suspensionStateBuilder.byProcessInstanceId(processInstanceId).suspend();
      }
    } else {
      // activating
      for (String processInstanceId : processInstanceIds) {
        suspensionStateBuilder.byProcessInstanceId(processInstanceId).activate();
      }
    }

    return null;
  }

}
