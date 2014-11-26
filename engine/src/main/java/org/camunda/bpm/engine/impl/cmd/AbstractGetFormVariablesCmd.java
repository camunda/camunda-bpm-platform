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
import java.util.Collection;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author  Daniel Meyer
 */
public abstract class AbstractGetFormVariablesCmd implements Command<VariableMap>, Serializable {

  private static final long serialVersionUID = 1L;

  public String resourceId;
  public Collection<String> formVariableNames;
  protected boolean deserializeObjectValues;

  public AbstractGetFormVariablesCmd(String resourceId, Collection<String> formVariableNames, boolean deserializeObjectValues) {
    this.resourceId = resourceId;
    this.formVariableNames = formVariableNames;
    this.deserializeObjectValues = deserializeObjectValues;
  }

  protected TypedValue createVariable(FormField formField, VariableScope variableScope) {
    TypedValue value = formField.getValue();

    if(value != null) {
      return value;
    }
    else {
      return null;
    }

  }

}