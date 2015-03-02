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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmd.AbstractInstantiationCmd;
import org.camunda.bpm.engine.runtime.ProcessInstanceActivityInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceActivityInstantiationBuilderImpl implements ProcessInstanceActivityInstantiationBuilder {

  protected ProcessInstanceModificationBuilderImpl builder;
  protected AbstractInstantiationCmd currentInstantiation;

  public ProcessInstanceActivityInstantiationBuilderImpl(ProcessInstanceModificationBuilderImpl builder,
      AbstractInstantiationCmd currentInstantiation) {
    this.builder = builder;
    this.currentInstantiation = currentInstantiation;
  }

  public ProcessInstanceActivityInstantiationBuilder setVariable(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);
    ensureNotNull(NotValidException.class, "No activity to start specified", "variable", currentInstantiation);

    currentInstantiation.addVariable(name, value);
    return this;
  }

  public ProcessInstanceActivityInstantiationBuilder setVariableLocal(String name, Object value) {
    ensureNotNull(NotValidException.class, "Variable name must not be null", "name", name);
    ensureNotNull(NotValidException.class, "No activity to start specified", "variableLocal", currentInstantiation);

    currentInstantiation.addVariableLocal(name, value);
    return this;
  }

  public ProcessInstanceModificationBuilder cancelActivityInstance(String activityInstanceId) {
    return builder.cancelActivityInstance(activityInstanceId);
  }

  public ProcessInstanceModificationBuilder cancelAllInActivity(String activityId) {
    return builder.cancelAllInActivity(activityId);
  }

  public ProcessInstanceActivityInstantiationBuilder startBeforeActivity(String activityId) {
    return builder.startBeforeActivity(activityId);
  }

  public ProcessInstanceActivityInstantiationBuilder startBeforeActivity(String activityId, String ancestorActivityInstanceId) {
    return builder.startBeforeActivity(activityId, ancestorActivityInstanceId);
  }

  public ProcessInstanceActivityInstantiationBuilder startAfterActivity(String activityId) {
    return builder.startAfterActivity(activityId);
  }

  public ProcessInstanceActivityInstantiationBuilder startAfterActivity(String activityId, String ancestorActivityInstanceId) {
    return builder.startBeforeActivity(activityId, ancestorActivityInstanceId);
  }

  public ProcessInstanceActivityInstantiationBuilder startTransition(String transitionId) {
    return builder.startTransition(transitionId);
  }

  public ProcessInstanceActivityInstantiationBuilder startTransition(String transitionId, String ancestorActivityInstanceId) {
    return builder.startTransition(transitionId, ancestorActivityInstanceId);
  }

  public void execute() {
    builder.execute();
  }

  public void execute(boolean skipCustomListeners, boolean skipIoMappings) {
    builder.execute(skipCustomListeners, skipIoMappings);
  }

}
