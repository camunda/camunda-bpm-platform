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
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.VariableType;
import org.camunda.bpm.engine.impl.variable.VariableTypes;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author  Daniel Meyer
 */
public abstract class AbstractGetFormVariablesCmd implements Command<Map<String, VariableInstance>> {

  public String resourceId;
  public Collection<String> formVariableNames;

  public AbstractGetFormVariablesCmd(String resourceId, Collection<String> formVariableNames) {
    this.resourceId = resourceId;
    this.formVariableNames = formVariableNames;
  }

  /**
   * Converts a FormField into a VariableInstance. Reads name, type information and default value.
   *
   */
  protected VariableInstance createVariable(FormField formField) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();

    // name
    variableInstance.setName(formField.getId());

    // type
    VariableTypes variableTypes = Context.getProcessEngineConfiguration()
      .getVariableTypes();

    VariableType variableType = variableTypes.getVariableType(formField.getTypeName());
    if(variableType == null) {
      throw new ProcessEngineException("Unsupported variable type '"+formField.getTypeName()+ "'.");
    }
    variableInstance.setType(variableType);

    // value
    Object defaultValue = formField.getDefaultValue();
    if(defaultValue != null) {
      variableInstance.setValue(defaultValue);
    }

    return variableInstance;
  }

}