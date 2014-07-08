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
package org.camunda.bpm.engine.impl.core.variable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public interface CoreVariableStore {

  Collection<CoreVariableInstance> getVariableInstancesValues();

  CoreVariableInstance getVariableInstance(String variableName);

  Set<String> getVariableNames();

  boolean isEmpty();

  boolean containsVariableInstance(String variableName);

  CoreVariableInstance removeVariableInstance(String variableName, CoreVariableScope sourceActivityExecution);

  void setVariableInstanceValue(CoreVariableInstance variableInstance, Object value, CoreVariableScope sourceActivityExecution);

  CoreVariableInstance createVariableInstance(String variableName, Object value, CoreVariableScope sourceActivityExecution);

  void clearForNewValue(CoreVariableInstance variableInstance, Object newValue);

  Map<String, CoreVariableInstance> getVariableInstances();

}