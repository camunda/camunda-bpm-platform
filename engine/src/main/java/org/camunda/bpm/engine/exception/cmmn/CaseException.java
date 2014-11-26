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
package org.camunda.bpm.engine.exception.cmmn;

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>This is exception is thrown when something happens in the execution
 * of a case instance.</p>
 *
 * @author Roman Smirnov
 *
 */
public class CaseException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;

  public CaseException() {
    super();
  }

  public CaseException(String message, Throwable cause) {
    super(message, cause);
  }

  public CaseException(String message) {
    super(message);
  }

  public CaseException(Throwable cause) {
    super(cause);
  }

}
