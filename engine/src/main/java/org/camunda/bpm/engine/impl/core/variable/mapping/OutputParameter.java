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
package org.camunda.bpm.engine.impl.core.variable.mapping;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;

/**
 *
 * <pre>
 *   +--------------+
 *   |              |
 *   |  inner scope ------> outer scope
 *   |              |
 *   +--------------+
 * </pre>
 *
 * @author Daniel Meyer
 *
 */
public class OutputParameter extends IoParameter {

  private final static Logger LOG = Logger.getLogger(OutputParameter.class.getName());

  public OutputParameter(String name, ParameterValueProvider valueProvider) {
    super(name, valueProvider);
  }

  protected void execute(AbstractVariableScope innerScope, AbstractVariableScope outerScope) {

    // get value from inner scope
    Object value = valueProvider.getValue(innerScope);

    if(LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "Mapping value '"+value+"' from inner scope '"+innerScope
          +"' to variable '"+name+ "' in outer scope '"+outerScope+"'.");
    }

    // set variable in outer scope
    outerScope.setVariable(name, value);
  }

}
