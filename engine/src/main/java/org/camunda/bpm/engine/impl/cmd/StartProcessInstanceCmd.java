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

import org.camunda.bpm.engine.impl.ProcessInstantiationBuilderImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class StartProcessInstanceCmd implements Command<ProcessInstance>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final ProcessInstantiationBuilderImpl instantiationBuilder;

  public StartProcessInstanceCmd(ProcessInstantiationBuilderImpl instantiationBuilder) {
    this.instantiationBuilder = instantiationBuilder;
  }

  public ProcessInstance execute(CommandContext commandContext) {

    ProcessDefinitionEntity processDefinition = new GetDeployedProcessDefinitionCmd(instantiationBuilder, false).execute(commandContext);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateProcessInstance(processDefinition);
    }

    // Start the process instance
    ExecutionEntity processInstance = processDefinition.createProcessInstance(instantiationBuilder.getBusinessKey(),
        instantiationBuilder.getCaseInstanceId());
    processInstance.start(instantiationBuilder.getVariables());
    return processInstance;
  }

}
