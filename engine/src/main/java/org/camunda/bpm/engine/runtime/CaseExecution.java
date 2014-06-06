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

/**
 * @author Roman Smirnov
 *
 */
public interface CaseExecution {

  /**
   * The unique identifier of the execution.
   */
  String getId();

  /** Id of the root of the execution tree representing the case instance.
   * It is the same as {@link #getId()} if this execution is the case instance. */
  String getCaseInstanceId();

  /**
   * Returns true if the case instance is active.
   */
  boolean isActive();

  /**
   * Returns true if the case instance is enabled.
   */
  boolean isEnabled();

}
