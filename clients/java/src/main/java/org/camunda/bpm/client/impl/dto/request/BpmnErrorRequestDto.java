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
package org.camunda.bpm.client.impl.dto.request;

import org.camunda.bpm.client.impl.dto.AbstractDto;

/**
 * @author Tassilo Weidner
 */
public class BpmnErrorRequestDto extends AbstractDto {

  private String workerId;
  private String errorCode;

  public BpmnErrorRequestDto(String workerId, String errorCode) {
    this.workerId = workerId;
    this.errorCode = errorCode;
  }

  public String getWorkerId() {
    return workerId;
  }

  public String getErrorCode() {
    return errorCode;
  }

}
