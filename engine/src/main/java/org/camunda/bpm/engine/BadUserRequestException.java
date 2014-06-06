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

package org.camunda.bpm.engine;

/**
 * <p>Exception resulting from a bad user request. A bad user request is
 * an interaction where the user requests some non-existing state or
 * attempts to perform an illegal action on some entity.</p>
 *
 * <p><strong>Examples:</strong>
 * <ul>
 *  <li>cancelling a non-existing process instance</li>
 *  <li>triggering a suspended execution...</li>
 * </ul>
 * </p>
 *
 * @author Sebastian Menski
 */
public class BadUserRequestException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;

  public BadUserRequestException() {
  }

  public BadUserRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public BadUserRequestException(String message) {
    super(message);
  }

  public BadUserRequestException(Throwable cause) {
    super(cause);
  }

}
