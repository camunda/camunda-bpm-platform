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


import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class ModifyProcessInstanceCmd implements Command<Void> {

  protected ProcessInstanceModificationBuilderImpl builder;

  public ModifyProcessInstanceCmd(ProcessInstanceModificationBuilderImpl processInstanceModificationBuilder) {
    this.builder = processInstanceModificationBuilder;
  }

  public Void execute(CommandContext commandContext) {
    String processInstanceId = builder.getProcessInstanceId();

    for (AbstractProcessInstanceModificationCommand instruction : builder.getModificationOperations()) {
      instruction.setSkipCustomListeners(builder.isSkipCustomListeners());
      instruction.setSkipIoMappings(builder.isSkipIoMappings());
      instruction.execute(commandContext);
    }

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    if (processInstance.getExecutions().isEmpty() && processInstance.getActivity() == null) {
      processInstance.deleteCascade("Cancellation due to process instance modification", builder.isSkipCustomListeners(), builder.isSkipIoMappings());
    }

    return null;
  }


}
