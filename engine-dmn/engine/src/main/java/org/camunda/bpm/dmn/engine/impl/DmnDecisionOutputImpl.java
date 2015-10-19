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

import java.util.LinkedHashMap;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;

public class DmnDecisionOutputImpl extends LinkedHashMap<String, Object> implements DmnDecisionOutput {

  public static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unchecked")
  public <T> T getValue(String name) {
    return (T) get(name);
  }

  @SuppressWarnings("unchecked")
  public <T> T getFirstValue() {
    if (!isEmpty()) {
      return (T) values().iterator().next();
    }
    else {
      return null;
    }
  }

  public <T> T getSingleValue() {
    if (size() > 1) {
      throw LOG.decisionOutputHasMoreThanOneValue(this);
    }
    else {
      return getFirstValue();
    }
  }

}
