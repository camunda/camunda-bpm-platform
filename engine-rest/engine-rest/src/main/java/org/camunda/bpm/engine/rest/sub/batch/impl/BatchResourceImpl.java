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

package org.camunda.bpm.engine.rest.sub.batch.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.management.JobDefinitionSuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.batch.BatchResource;

public class BatchResourceImpl implements BatchResource {

  protected ProcessEngine processEngine;
  protected String batchId;

  public BatchResourceImpl(ProcessEngine processEngine, String batchId) {
    this.processEngine = processEngine;
    this.batchId = batchId;
  }

  public BatchDto getBatch() {
    Batch batch = processEngine.getManagementService()
      .createBatchQuery()
      .batchId(batchId)
      .singleResult();

    if (batch == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Batch with id '" + batchId + "' does not exist");
    }

    return BatchDto.fromBatch(batch);
  }

  public void updateSuspensionState(SuspensionStateDto suspensionState) {
    if (suspensionState.getSuspended()) {
      suspendBatch();
    }
    else {
      activateBatch();
    }
  }

  protected void suspendBatch() {
    try {
      processEngine.getManagementService().suspendBatchById(batchId);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Unable to suspend batch with id '" + batchId + "'");
    }
  }

  protected void activateBatch() {
    try {
      processEngine.getManagementService().activateBatchById(batchId);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Unable to activate batch with id '" + batchId + "'");
    }
  }

  public void deleteBatch(boolean cascade) {
    try {
      processEngine.getManagementService()
        .deleteBatch(batchId, cascade);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Unable to delete batch with id '" + batchId + "'");
    }
  }

}
