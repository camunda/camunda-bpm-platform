/*
 * Copyright © 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.client.task.impl.dto;

import org.camunda.bpm.client.impl.RequestDto;

/**
 * @author Tassilo Weidner
 */
public class FailureRequestDto extends RequestDto {

  protected String errorMessage;
  protected String errorDetails;
  protected int retries;
  protected long retryTimeout;

  public FailureRequestDto(String workerId, String errorMessage, String errorDetails, int retries, long retryTimeout) {
    super(workerId);
    this.errorMessage = errorMessage;
    this.errorDetails = errorDetails;
    this.retries = retries;
    this.retryTimeout = retryTimeout;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getErrorDetails() {
    return errorDetails;
  }

  public int getRetries() {
    return retries;
  }

  public long getRetryTimeout() {
    return retryTimeout;
  }

}
