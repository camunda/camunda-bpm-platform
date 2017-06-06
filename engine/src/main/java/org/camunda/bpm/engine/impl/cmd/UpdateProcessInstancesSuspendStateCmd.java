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
import org.camunda.bpm.engine.impl.UpdateProcessInstancesSuspensionStationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.runtime.UpdateProcessInstanceSuspensionStateBuilderImpl;

public class UpdateProcessInstancesSuspendStateCmd extends AbstractUpdateProcessInstancesSuspendStateCmd<Void> {


  public UpdateProcessInstancesSuspendStateCmd(CommandExecutor commandExecutor, UpdateProcessInstancesSuspensionStationBuilderImpl builder) {
    super(commandExecutor, builder);
  }




  public Void execute(CommandContext commandContext) {

    Collection<String> processInstanceIds = collectProcessInstanceIds();
    boolean suspendstate = builder.getSuspendState();

    writeUserOperationLog(commandContext, processInstanceIds.size(), false, suspendstate);

    UpdateProcessInstanceSuspensionStateBuilderImpl suspensionStateBuilder = new UpdateProcessInstanceSuspensionStateBuilderImpl(commandExecutor);
    if (suspendstate) {
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
