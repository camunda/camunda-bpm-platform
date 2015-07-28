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

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;

import java.util.HashMap;

public class DmnDecisionOutputImpl extends HashMap<String, Object> implements DmnDecisionOutput {

  @SuppressWarnings("unchecked")
  public <T> T getValue(String name) {
    return (T) super.get(name);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    if (!isEmpty()) {
      return (T) values().iterator().next();
    }
    else {
      return null;
    }
  }

}
