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

package org.camunda.bpm.engine.rest.sub.history.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.rest.dto.history.batch.HistoricBatchDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricBatchResource;

public class HistoricBatchResourceImpl implements HistoricBatchResource {

  protected ProcessEngine processEngine;
  protected String batchId;

  public HistoricBatchResourceImpl(ProcessEngine processEngine, String batchId) {
    this.processEngine = processEngine;
    this.batchId = batchId;
  }

  public HistoricBatchDto getHistoricBatch() {
    HistoricBatch batch = processEngine.getHistoryService()
      .createHistoricBatchQuery()
      .batchId(batchId)
      .singleResult();

    if (batch == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic batch with id '" + batchId + "' does not exist");
    }

    return HistoricBatchDto.fromBatch(batch);
  }

  public void deleteHistoricBatch() {
    try {
      processEngine.getHistoryService()
        .deleteHistoricBatch(batchId);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "Unable to delete historic batch with id '" + batchId + "'");
    }
  }

}
