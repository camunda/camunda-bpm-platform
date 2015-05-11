/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.Map;

import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Sebastian Menski
 */
public class ExecutionStartContext {

  protected boolean delayFireHistoricVariableEvents;

  protected InstantiationStack instantiationStack;
  protected Map<String, Object> variables;
  protected Map<String, Object> variablesLocal;

  public ExecutionStartContext() {
    this(true);
  }

  public ExecutionStartContext(boolean delayFireHistoricVariableEvents) {
    this.delayFireHistoricVariableEvents = delayFireHistoricVariableEvents;
  }

  public void executionStarted(PvmExecutionImpl execution) {

    if (execution instanceof ExecutionEntity && delayFireHistoricVariableEvents) {
      ExecutionEntity executionEntity = (ExecutionEntity) execution;
      executionEntity.fireHistoricVariableInstanceCreateEvents();
    }

    PvmExecutionImpl parent = execution;
    while (parent != null && parent.getExecutionStartContext() != null) {
      parent.disposeExecutionStartContext();
      parent = parent.getParent();
    }
  }

  public void applyVariables(CoreExecution execution) {
    execution.setVariables(variables);
    execution.setVariablesLocal(variablesLocal);
  }

  public boolean isDelayFireHistoricVariableEvents() {
    return delayFireHistoricVariableEvents;
  }

  public InstantiationStack getInstantiationStack() {
    return instantiationStack;
  }

  public void setInstantiationStack(InstantiationStack instantiationStack) {
    this.instantiationStack = instantiationStack;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public void setVariablesLocal(Map<String, Object> variablesLocal) {
    this.variablesLocal = variablesLocal;
  }
}
