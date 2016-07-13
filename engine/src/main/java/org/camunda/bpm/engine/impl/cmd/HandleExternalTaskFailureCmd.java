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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 * @author Askar Akhmerov
 */
public class HandleExternalTaskFailureCmd extends HandleExternalTaskCmd {

  protected String errorMessage;
  protected String errorDetails;
  protected long retryDuration;
  protected int retries;

  public HandleExternalTaskFailureCmd(String externalTaskId, String workerId,
                                      String errorMessage, int retries, long retryDuration) {
    this(externalTaskId,workerId,errorMessage,null,retries,retryDuration);
  }

  /**
   * Overloaded constructor to support short and full error messages
   *
   * @param externalTaskId
   * @param workerId
   * @param errorMessage
   * @param errorDetails
   * @param retries
   * @param retryDuration
   */
  public HandleExternalTaskFailureCmd(String externalTaskId, String workerId,
                                      String errorMessage, String errorDetails, int retries, long retryDuration) {
    super(externalTaskId, workerId);
    this.errorMessage = errorMessage;
    this.errorDetails = errorDetails;
    this.retries = retries;
    this.retryDuration = retryDuration;
  }

  @Override
  public void execute(ExternalTaskEntity externalTask) {
    externalTask.failed(errorMessage, errorDetails, retries, retryDuration);
  }

  @Override
  protected void validateInput() {
    super.validateInput();
    EnsureUtil.ensureGreaterThanOrEqual("retries", retries, 0);
    EnsureUtil.ensureGreaterThanOrEqual("retryDuration", retryDuration, 0);
  }

  @Override
  public String getErrorMessageOnWrongWorkerAccess() {
    return "Failure of External Task " + externalTaskId + " cannot be reported by worker '" + workerId;
  }
}
