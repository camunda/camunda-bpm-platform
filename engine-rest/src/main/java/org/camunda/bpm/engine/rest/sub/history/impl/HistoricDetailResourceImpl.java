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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricDetailResource;
import org.camunda.bpm.engine.variable.type.ValueType;

import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

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

  public HistoricDetailDto getDetail(boolean deserializeObjectValue) {
    HistoricDetailQuery query = baseQuery().disableBinaryFetching();

    if (!deserializeObjectValue) {
      query.disableCustomObjectDeserialization();
    }

    HistoricDetail detail = query.singleResult();

    if(detail != null) {
      return HistoricDetailDto.fromHistoricDetail(detail);

    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic detail with Id '"+detailId + "' does not exist.");

    }
  }

  public InputStream getBinaryVariable() {
    HistoricDetail historicDetail = baseQuery()
        .disableCustomObjectDeserialization()
        .singleResult();
    if(historicDetail != null) {
      if(!(historicDetail instanceof HistoricVariableUpdate)) {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Historic detail with Id '"+detailId + "' is not a variable update.");
      }

      HistoricVariableUpdate update = (HistoricVariableUpdate) historicDetail;

      if (ValueType.BYTES.getName().equals(update.getTypeName())) {
        byte[] valueBytes = (byte[]) update.getValue();
        if (valueBytes == null) {
          valueBytes = new byte[0];
        }

        return new ByteArrayInputStream(valueBytes);
      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST,
            String.format("Value of variable %s is not a binary value.", detailId));
      }

    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic detail instance with Id '"+detailId + "' does not exist.");
    }
  }

  protected HistoricDetailQuery baseQuery() {
    return engine.getHistoryService()
        .createHistoricDetailQuery()
        .detailId(detailId);
  }

}
