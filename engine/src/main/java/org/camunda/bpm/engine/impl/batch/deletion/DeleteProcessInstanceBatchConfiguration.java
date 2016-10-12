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

package org.camunda.bpm.engine.impl.batch.deletion;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;

import java.util.List;

/**
 * Configuration object that is passed to the Job that will actually perform execution of
 * deletion.
 * <p>
 * This object will be serialized and persisted as run will be performed asynchronously.
 *
 * @author Askar Akhmerov
 * @see org.camunda.bpm.engine.impl.batch.deletion.DeleteProcessInstanceBatchConfigurationJsonConverter
 */
public class DeleteProcessInstanceBatchConfiguration extends BatchConfiguration {
  protected String deleteReason;

  public DeleteProcessInstanceBatchConfiguration(List<String> ids) {
    super(ids);
  }

  public DeleteProcessInstanceBatchConfiguration(List<String> ids, String deleteReason) {
    super(ids);
    this.deleteReason = deleteReason;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }

}
