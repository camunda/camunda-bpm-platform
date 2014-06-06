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
package org.camunda.bpm.engine.rest.sub.history.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricDetailResource;

/**
 * @author Daniel Meyer
 *
 */
public class HistoricDetailResourceImpl implements HistoricDetailResource {

  protected String detailId;
  protected ProcessEngine engine;

  public HistoricDetailResourceImpl(String detailId, ProcessEngine engine) {
    this.detailId = detailId;
    this.engine = engine;
  }

  public HistoricDetailDto getDetail() {
    HistoricDetail detail = baseQuery()
      .disableBinaryFetching()
      .singleResult();
    if(detail != null) {
      return HistoricDetailDto.fromHistoricDetail(detail);

    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic detail with Id '"+detailId + "' does not exist.");

    }
  }

  public InputStream getBinaryVariable() {
    HistoricDetail variableInstance = baseQuery()
        .singleResult();
    if(variableInstance == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic detail with Id '"+detailId + "' does not exist.");

    } else if(!(variableInstance instanceof HistoricVariableUpdate)) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Historic detail with Id '"+detailId + "' is not a variable update.");

    } else {
      Object value = ((HistoricVariableUpdate) variableInstance).getValue();
      if(value instanceof byte[]) {
        return new ByteArrayInputStream((byte[]) value);

      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Historic detail with Id '"+detailId + "' is not a binary variable.");

      }

    }
  }

  protected HistoricDetailQuery baseQuery() {
    return engine.getHistoryService()
        .createHistoricDetailQuery()
        .detailId(detailId);
  }

}
