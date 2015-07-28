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
package org.camunda.bpm.engine.exception.dmn;

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>This exception is thrown when something happens related to a decision.</p>
 */
public class DecisionException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;

  public DecisionException() {
    super();
  }

  public DecisionException(String message, Throwable cause) {
    super(message, cause);
  }

  public DecisionException(String message) {
    super(message);
  }

  public DecisionException(Throwable cause) {
    super(cause);
  }

}
