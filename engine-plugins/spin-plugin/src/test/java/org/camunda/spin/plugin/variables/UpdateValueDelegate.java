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
package org.camunda.spin.plugin.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Thorben Lindhauer
 *
 */
public class UpdateValueDelegate implements JavaDelegate, Serializable {

  private static final long serialVersionUID = 1L;

  public static final String STRING_PROPERTY = "a string value";

  public void execute(DelegateExecution execution) throws Exception {
    TypedValue typedValue = execution.getVariableTyped("listVar");
    List<JsonSerializable> var = (List<JsonSerializable>) typedValue.getValue();
    JsonSerializable newElement = new JsonSerializable();
    newElement.setStringProperty(STRING_PROPERTY);
    // implicit update of the list, so no execution.setVariable call
    var.add(newElement);

  }

}
