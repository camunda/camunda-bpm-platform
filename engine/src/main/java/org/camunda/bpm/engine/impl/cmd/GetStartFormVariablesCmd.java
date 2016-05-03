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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 * @author Daniel Meyer
 *
 */
public class GetStartFormVariablesCmd extends AbstractGetFormVariablesCmd {

  private static final long serialVersionUID = 1L;

  public GetStartFormVariablesCmd(String resourceId, Collection<String> formVariableNames, boolean deserializeObjectValues) {
    super(resourceId, formVariableNames, deserializeObjectValues);
  }

  public VariableMap execute(final CommandContext commandContext) {
    StartFormData startFormData = commandContext.runWithoutAuthorization(new Callable<StartFormData>() {
      public StartFormData call() throws Exception {
        return new GetStartFormCmd(resourceId).execute(commandContext);
      }
    });

    ProcessDefinition definition = startFormData.getProcessDefinition();
    checkGetStartFormVariables((ProcessDefinitionEntity) definition, commandContext);

    VariableMap result = new VariableMapImpl();

    for (FormField formField : startFormData.getFormFields()) {
      if(formVariableNames == null || formVariableNames.contains(formField.getId())) {
        result.put(formField.getId(), createVariable(formField, null));
      }
    }

    return result;
  }

  protected void checkGetStartFormVariables(ProcessDefinitionEntity definition, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessDefinition(definition);
    }
  }
}
