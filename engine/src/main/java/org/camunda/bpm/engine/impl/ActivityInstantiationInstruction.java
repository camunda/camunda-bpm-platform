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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityInstantiationInstruction {

  protected String activityId;

  protected VariableMap variables;
  protected VariableMap variablesLocal;


  public ActivityInstantiationInstruction(String activityId) {
    this.activityId = activityId;
    this.variables = new VariableMapImpl();
    this.variablesLocal = new VariableMapImpl();

  }

  public void addVariable(String name, Object value) {
    this.variables.put(name, value);
  }

  public void addVariableLocal(String name, Object value) {
    this.variablesLocal.put(name, value);
  }

  public String getActivityId() {
    return activityId;
  }

  public VariableMap getVariables() {
    return variables;
  }

  public VariableMap getVariablesLocal() {
    return variablesLocal;
  }
}
