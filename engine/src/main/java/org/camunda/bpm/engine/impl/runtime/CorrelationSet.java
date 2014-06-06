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

package org.camunda.bpm.engine.impl.runtime;

import java.util.Map;

public class CorrelationSet {

  protected final String businessKey;
  protected final Map<String, Object> correlationKeys;
  protected String processInstanceId;

  public CorrelationSet(String businessKey, String processInstanceId, Map<String, Object> correlationKeys) {
    this.businessKey = businessKey;
    this.processInstanceId = processInstanceId;
    this.correlationKeys = correlationKeys;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public Map<String, Object> getCorrelationKeys() {
    return correlationKeys;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }
}
