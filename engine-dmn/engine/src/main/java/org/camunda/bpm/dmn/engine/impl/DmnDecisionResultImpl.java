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

package org.camunda.bpm.dmn.engine.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;

public class DmnDecisionResultImpl extends ArrayList<DmnDecisionOutput> implements DmnDecisionResult, Serializable {

  public static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  private static final long serialVersionUID = 1L;

  public DmnDecisionOutput getFirstOutput() {
    if (size() > 0) {
      return get(0);
    }
    else {
      return null;
    }
  }

  public DmnDecisionOutput getSingleOutput() {
    if (size() == 1) {
      return get(0);
    }
    else if (isEmpty()) {
      return null;
    }
    else {
      throw LOG.decisionResultHasMoreThanOneOutput(this);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> collectOutputValues(String outputName) {
    List<T> outputValues = new ArrayList<T>();
    for (DmnDecisionOutput decisionOutput : this) {
      Object value = decisionOutput.getValue(outputName);
      outputValues.add((T) value);
    }
    return outputValues;
  }

}
