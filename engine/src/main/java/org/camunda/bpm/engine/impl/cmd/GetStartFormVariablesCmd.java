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
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author Daniel Meyer
 *
 */
public class GetStartFormVariablesCmd extends AbstractGetFormVariablesCmd {

  public GetStartFormVariablesCmd(String resourceId, Collection<String> formVariableNames) {
    super(resourceId, formVariableNames);
  }

  public Map<String, VariableInstance> execute(CommandContext commandContext) {

    StartFormData startFormData = new GetStartFormCmd(resourceId)
      .execute(commandContext);

    Map<String, VariableInstance> result = new HashMap<String, VariableInstance>();

    for (FormField formField : startFormData.getFormFields()) {
      if(formVariableNames == null || formVariableNames.contains(formField.getId())) {
        result.put(formField.getId(), createVariable(formField));
      }
    }

    return result;
  }

}
