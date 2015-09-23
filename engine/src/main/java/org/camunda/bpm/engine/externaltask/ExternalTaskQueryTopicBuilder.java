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

/**
 * @author Thorben Lindhauer
 *
 */
public interface ExternalTaskQueryTopicBuilder extends ExternalTaskQueryBuilder {

  /**
   * Define variables to fetch with all tasks for the current topic. Calling
   * this method multiple times overrides the previously specified variables.
   *
   * @param variables the variable names to fetch
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder variables(String... variables);

  /**
   * Define variables to fetch with all tasks for the current topic. Calling
   * this method multiple times overrides the previously specified variables.
   *
   * @param variables the variable names to fetch
   * @return this builder
   */
  public ExternalTaskQueryTopicBuilder variables(List<String> variables);
}
