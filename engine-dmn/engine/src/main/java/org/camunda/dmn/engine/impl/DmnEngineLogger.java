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

package org.camunda.dmn.engine.impl;

import org.camunda.dmn.engine.DmnEngineException;

public class DmnEngineLogger extends DmnLogger {

  public DmnEngineException outputDoesNotContainAnyComponent() {
    return new DmnEngineException(exceptionMessage("001", "The DMN output doesn't contain any component."));
  }

  public DmnEngineException unableToFindOutputComponentWithName(String name) {
    return new DmnEngineException(exceptionMessage("002", "Unable to find output component with name '{}'.", name));
  }

}
