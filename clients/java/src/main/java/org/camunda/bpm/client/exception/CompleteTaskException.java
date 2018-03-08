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
package org.camunda.bpm.client.exception;

import org.camunda.bpm.client.task.ExternalTaskService;

/**
 * <p>Complete task exception is thrown if an exception occurred while executing {@link ExternalTaskService#complete()}</p>
 *
 * @author Tassilo Weidner
 */
public class CompleteTaskException extends CamundaClientException {
  public CompleteTaskException(String message) {
    super(message);
  }
}
