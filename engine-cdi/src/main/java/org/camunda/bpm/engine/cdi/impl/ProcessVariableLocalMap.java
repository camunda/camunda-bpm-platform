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
package org.camunda.bpm.engine.cdi.impl;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Allows to expose the local process variables of the current business process as a
 * java.util.Map<String,Object>
 * <p/>
 * The map delegates changes to
 * {@link BusinessProcess#setVariableLocal(String, Object)} and
 * {@link BusinessProcess#getVariableLocal(String)}, so that they are not flushed
 * prematurely.
 * 
 * @author Michael Scholz
 */
public class ProcessVariableLocalMap extends AbstractVariableMap {
  
  @Override
  protected Object getVariable(String variableName) {
    return businessProcess.getVariableLocal(variableName);
  }

  @Override
  protected <T extends TypedValue> T getVariableTyped(String variableName) {
    return businessProcess.getVariableLocalTyped(variableName);
  }

  @Override
  protected void setVariable(String variableName, Object value) {
    businessProcess.setVariableLocal(variableName, value);
  }
}
