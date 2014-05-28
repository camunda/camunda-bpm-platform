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
package org.camunda.bpm.engine.delegate;

import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

/**
 * Implemented by classes which provide access to the {@link CmmnModelInstance}
 * and the currently executed {@link CmmnElement}.
 *
 * @author Roman Smirnov
 *
 */
public interface CmmnModelExecutionContext {

  /**
   * Returns the {@link CmmnModelInstance} for the currently executed Cmmn Model
   *
   * @return the current {@link CmmnModelInstance}
   */
  CmmnModelInstance getCmmnModelInstance();

  /**
   * <p>Returns the currently executed Element in the Cmmn Model. This method returns a {@link CmmnElement} which may be casted
   * to the concrete type of the Cmmn Model Element currently executed.</p>
   *
   * @return the {@link CmmnElement} corresponding to the current Cmmn Model Element
   */
  CmmnElement getCmmnModelElementInstance();

}
