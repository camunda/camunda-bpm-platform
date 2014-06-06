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
package org.camunda.bpm.engine.runtime;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>A fluent builder to create a new case instance.</p>
 *
 * @author Roman Smirnov
 *
 */
public interface CaseInstanceBuilder {

  /**
   * A business key can be provided to associate the case instance with a
   * certain identifier that has a clear business meaning. This business key can
   * then be used to easily look up that case instance, see
   * {@link CaseInstanceQuery#caseInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given case definition WHEN you have added
   * a database constraint for it.
   * In this case, only case instance from different case definition are allowed to have the
   * same business key and the combination of caseDefinitionKey-businessKey must be unique.
   *
   * @param businessKey
   *          a key that uniquely identifies the case instance in the context
   *          of the given case definition.
   *
   * @return the builder
   */
  CaseInstanceBuilder businessKey(String businessKey);

  /**
   * <p>Pass a variable to the case instance.</p>
   *
   * <p>Invoking this method multiple times allows passing multiple variables.</p>
   *
   * @param variableName the name of the variable to set
   * @param variableValue the value of the variable to set
   * @return the builder
   */
  CaseInstanceBuilder setVariable(String variableName, Object variableValue);

  /**
   * <p>Pass a map of variables to the case instance.</p>
   *
   * <p>Invoking this method multiple times allows passing multiple variables.</p>
   *
   * @param variables the map of variables
   * @return the builder
   */
  CaseInstanceBuilder setVariables(Map<String, Object> variables);

  /**
   * Creates a new {@link CaseInstance}, which will be in the ACTIVE state.
   *
   * @throws ProcessEngineException when no case definition is deployed with the given key or id.
   */
  CaseInstance create();

}
