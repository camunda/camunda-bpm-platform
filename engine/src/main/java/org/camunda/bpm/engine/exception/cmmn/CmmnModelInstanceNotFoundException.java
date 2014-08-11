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

import org.camunda.bpm.model.cmmn.CmmnModelInstance;


/**
 * <p>This is exception is thrown when a {@link CmmnModelInstance} is not found.</p>
 *
 * @author Roman Smirnov
 *
 */
public class CmmnModelInstanceNotFoundException extends CaseException {

  private static final long serialVersionUID = 1L;

  public CmmnModelInstanceNotFoundException() {
    super();
  }

  public CmmnModelInstanceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public CmmnModelInstanceNotFoundException(String message) {
    super(message);
  }

  public CmmnModelInstanceNotFoundException(Throwable cause) {
    super(cause);
  }

}
