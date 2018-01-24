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
package org.camunda.bpm.engine.impl.core;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;

/**
 * @author Daniel Meyer
 *
 */
public class CoreLogger extends ProcessEngineLogger {

  public void debugMappingValueFromOuterScopeToInnerScope(Object value, AbstractVariableScope outerScope, String name, AbstractVariableScope innerScope) {
    logDebug(
        "001",
        "Mapping value '{} from outer scope '{}' to variable '{}' in inner scope '{}'.",
        value, outerScope, name, innerScope);
  }

  public void debugMappingValuefromInnerScopeToOuterScope(Object value, AbstractVariableScope innerScope, String name, AbstractVariableScope outerScope) {
    logDebug(
        "002",
        "Mapping value '{}' from inner scope '{}' to variable '{}' in outer scope '{}'.",
        value, innerScope, name, outerScope);
  }

  public void debugPerformingAtomicOperation(CoreAtomicOperation<?> atomicOperation, CoreExecution e) {
    logDebug(
        "003",
        "Performing atomic operation {} on {}", atomicOperation, e);
  }

  public ProcessEngineException duplicateVariableInstanceException(CoreVariableInstance variableInstance) {
    return new ProcessEngineException(exceptionMessage(
        "004",
        "Cannot add variable instance with name {}. Variable already exists",
        variableInstance.getName()
      ));
  }

  public ProcessEngineException missingVariableInstanceException(CoreVariableInstance variableInstance) {
    return new ProcessEngineException(exceptionMessage(
        "005",
        "Cannot update variable instance with name {}. Variable does not exist",
        variableInstance.getName()
      ));
  }

  public ProcessEngineException transientVariableException(String variableName) {
    return new ProcessEngineException(exceptionMessage(
        "006",
        "Cannot set transient variable with name {}. Persisted variable already exists",
        variableName
      ));
  }

  public ProcessEngineException javaSerializationProhibitedException(String variableName) {
    return new ProcessEngineException(exceptionMessage(
        "007",
        "Cannot set variable with name {}. Java serialization format is prohibited",
        variableName
      ));
  }

}
