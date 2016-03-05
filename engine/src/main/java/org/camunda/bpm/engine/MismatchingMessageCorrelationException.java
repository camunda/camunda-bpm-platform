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

package org.camunda.bpm.engine;

import java.util.Map;

/**
 * @author Thorben Lindhauer
 */
public class MismatchingMessageCorrelationException extends
    ProcessEngineException {

  private static final long serialVersionUID = 1L;

  public MismatchingMessageCorrelationException(String message) {
    super(message);
  }

  public MismatchingMessageCorrelationException(String messageName, String reason) {
    this("Cannot correlate message '" + messageName + "': " + reason);
  }

  public MismatchingMessageCorrelationException(String messageName,
      String businessKey, Map<String, Object> correlationKeys) {
    this("Cannot correlate message '" + messageName + "' with process instance business key '" + businessKey
        + "' and correlation keys " + correlationKeys);
  }

  public MismatchingMessageCorrelationException(String messageName,
      String businessKey, Map<String, Object> correlationKeys, String reason) {
    this("Cannot correlate message '" + messageName + "' with process instance business key '" + businessKey
        + "' and correlation keys " + correlationKeys + ": " + reason);
  }
}
