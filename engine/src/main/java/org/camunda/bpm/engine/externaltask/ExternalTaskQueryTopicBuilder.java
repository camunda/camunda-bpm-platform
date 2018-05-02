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
package org.camunda.bpm.engine.externaltask;

import java.util.List;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public interface ExternalTaskQueryTopicBuilder extends ExternalTaskQueryBuilder {

  /**
   * Define variables to fetch with all tasks for the current topic. Calling
   * this method multiple times overrides the previously specified variables.
   *
   * @param variables the variable names to fetch, if null all variables will be fetched
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder variables(String... variables);

  /**
   * Define variables to fetch with all tasks for the current topic. Calling
   * this method multiple times overrides the previously specified variables.
   *
   * @param variables the variable names to fetch, if null all variables will be fetched
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder variables(List<String> variables);

  /**
   * Define a HashMap of variables and their values to filter correlated tasks.
   * Calling this method multiple times overrides the previously specified variables.
   *
   * @param variables a HashMap of the variable names (keys) and the values to filter by
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder processInstanceVariableEquals(Map<String, Object> variables);

  /**
   * Define a single variable and its name to filter tasks in a topic. Multiple calls to
   * this method add to the existing "variable filters".
   *
   * @param name the name of the variable you want to fetch and query by
   * @param value the value of the variable which you want to filter
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder processInstanceVariableEquals(String name, Object value);

  /**
   * Define business key value to filter external tasks by (Process Instance) Business Key.
   *
   * @param businessKey the value of the Business Key to filter by
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder businessKey(String businessKey);

  /**
   * Enable deserialization of variable values that are custom objects. By default, the query
   * will not attempt to deserialize the value of these variables.
   *
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder enableCustomObjectDeserialization();

  /**
   * Define whether only local variables will be fetched with all tasks for the current topic.
   *
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder localVariables();

}
